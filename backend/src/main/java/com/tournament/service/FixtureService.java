
package com.tournament.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tournament.dto.FixtureResponse;
import com.tournament.exception.FixtureGenerationException;
import com.tournament.exception.TournamentNotFoundException;
import com.tournament.model.Fixture;
import com.tournament.model.GroupTeam;
import com.tournament.model.Team;
import com.tournament.model.Tournament;
import com.tournament.model.TournamentGroup;
import com.tournament.repository.FixtureRepository;
import com.tournament.repository.GroupTeamRepository;
import com.tournament.repository.TournamentGroupRepository;
import com.tournament.repository.TournamentRepository;

@Service
public class FixtureService {

    private static final int MATCH_DURATION_MINUTES = 20;
    private static final int TEAM_REST_MINUTES = 20;

    private static final LocalTime DAILY_START =
            LocalTime.of(8, 0);

    private static final LocalTime LUNCH_START =
            LocalTime.of(12, 0);

    private static final LocalTime AFTER_LUNCH =
            LocalTime.of(13, 0);

    private static final LocalTime DAILY_END =
            LocalTime.of(17, 0);

    private final FixtureRepository fixtureRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentGroupRepository tournamentGroupRepository;
    private final GroupTeamRepository groupTeamRepository;

    public FixtureService(
            FixtureRepository fixtureRepository,
            TournamentRepository tournamentRepository,
            TournamentGroupRepository tournamentGroupRepository,
            GroupTeamRepository groupTeamRepository) {

        this.fixtureRepository = fixtureRepository;
        this.tournamentRepository = tournamentRepository;
        this.tournamentGroupRepository = tournamentGroupRepository;
        this.groupTeamRepository = groupTeamRepository;
    }

    @Transactional
    public List<FixtureResponse> generateFixtures(Long tournamentId) {

        // 1. Find the tournament.
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() ->
                        new TournamentNotFoundException(
                                "Tournament not found with ID: " + tournamentId
                        )
                );

        // 2. Check tournament dates.
        LocalDate startDate = tournament.getStartDate();
        LocalDate endDate = tournament.getEndDate();

        if (startDate == null || endDate == null) {
            throw new FixtureGenerationException(
                    "Tournament start date and end date are required"
            );
        }

        if (endDate.isBefore(startDate)) {
            throw new FixtureGenerationException(
                    "Tournament end date cannot be before start date"
            );
        }

        // 3. Check whether fixtures already exist.
        if (fixtureRepository.existsByTournamentGroup_Tournament_Id(
                tournamentId)) {

            throw new FixtureGenerationException(
                    "Fixtures have already been generated for this tournament"
            );
        }

        // 4. Retrieve tournament groups.
        List<TournamentGroup> groups =
                tournamentGroupRepository.findByTournament_Id(tournamentId);

        if (groups.isEmpty()) {
            throw new FixtureGenerationException(
                    "Generate tournament groups before generating fixtures"
            );
        }

        // 5. Generate all round-robin match pairings.
        List<Fixture> pendingFixtures = new ArrayList<>();

        for (TournamentGroup group : groups) {

            List<GroupTeam> groupTeams =
                    groupTeamRepository.findByTournamentGroup_Id(group.getId());

            if (groupTeams.size() < 2) {
                throw new FixtureGenerationException(
                        "Every group must contain at least two teams"
                );
            }

            for (int i = 0; i < groupTeams.size(); i++) {

                for (int j = i + 1; j < groupTeams.size(); j++) {

                    Team teamA = groupTeams.get(i).getTeam();
                    Team teamB = groupTeams.get(j).getTeam();

                    Fixture fixture = new Fixture();

                    fixture.setTournamentGroup(group);
                    fixture.setTeamA(teamA);
                    fixture.setTeamB(teamB);
                    fixture.setStatus("UPCOMING");

                    pendingFixtures.add(fixture);
                }
            }
        }

        // 6. Schedule matches across the tournament dates.
        List<Fixture> scheduledFixtures = new ArrayList<>();

        // Records when each team last finished a match.
        Map<Long, LocalDateTime> lastMatchEnd = new HashMap<>();

        Integer courtCount = tournament.getCourtCount();

        if (courtCount == null || courtCount < 1) {
            throw new FixtureGenerationException(
                    "Tournament must have at least one court"
            );
        }

        for (LocalDate date = startDate;
                !date.isAfter(endDate) && !pendingFixtures.isEmpty();
                date = date.plusDays(1)) {

            // Morning session: 8:00 AM to 12:00 PM.
            for (LocalTime time = DAILY_START;
                    time.isBefore(LUNCH_START)
                    && !pendingFixtures.isEmpty();
                    time = time.plusMinutes(MATCH_DURATION_MINUTES)) {

                scheduleMatchesAtTime(
                        date, time, courtCount,
                        pendingFixtures, scheduledFixtures, lastMatchEnd
                );
            }

            // Afternoon session: 1:00 PM to 5:00 PM.
            for (LocalTime time = AFTER_LUNCH;
                    time.isBefore(DAILY_END)
                    && !pendingFixtures.isEmpty();
                    time = time.plusMinutes(MATCH_DURATION_MINUTES)) {

                scheduleMatchesAtTime(
                        date, time, courtCount,
                        pendingFixtures, scheduledFixtures, lastMatchEnd
                );
            }
        }

        // 7. Reject generation if any matches remain unscheduled.
        if (!pendingFixtures.isEmpty()) {
            throw new FixtureGenerationException(
                    "Insufficient scheduling capacity. "
                    + pendingFixtures.size()
                    + " matches could not be scheduled. "
                    + "Consider adding tournament days or courts."
            );
        }

        // 8. Save all fixtures only after successful scheduling.
        List<Fixture> savedFixtures =
                fixtureRepository.saveAll(scheduledFixtures);

        return convertToResponses(savedFixtures);
    }

    private void scheduleMatchesAtTime(
            LocalDate date,
            LocalTime time,
            int courtCount,
            List<Fixture> pendingFixtures,
            List<Fixture> scheduledFixtures,
            Map<Long, LocalDateTime> lastMatchEnd) {

        LocalDateTime matchStart = LocalDateTime.of(date, time);

        // Fill available courts at this time.
        for (int court = 1;
                court <= courtCount && !pendingFixtures.isEmpty();
                court++) {

            for (int i = 0; i < pendingFixtures.size(); i++) {

                Fixture fixture = pendingFixtures.get(i);

                Long teamAId = fixture.getTeamA().getId();
                Long teamBId = fixture.getTeamB().getId();

                LocalDateTime teamAEnd = lastMatchEnd.get(teamAId);
                LocalDateTime teamBEnd = lastMatchEnd.get(teamBId);

                // Check the minimum rest period for both teams.
                boolean teamAReady =
                        teamAEnd == null
                        || !matchStart.isBefore(
                                teamAEnd.plusMinutes(TEAM_REST_MINUTES)
                        );

                boolean teamBReady =
                        teamBEnd == null
                        || !matchStart.isBefore(
                                teamBEnd.plusMinutes(TEAM_REST_MINUTES)
                        );

                if (teamAReady && teamBReady) {

                    fixture.setMatchDateTime(matchStart);
                    fixture.setCourtNumber(court);
                    fixture.setStatus("UPCOMING");

                    LocalDateTime matchEnd =
                            matchStart.plusMinutes(MATCH_DURATION_MINUTES);

                    lastMatchEnd.put(teamAId, matchEnd);
                    lastMatchEnd.put(teamBId, matchEnd);

                    scheduledFixtures.add(fixture);
                    pendingFixtures.remove(i);

                    break;
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<FixtureResponse> getFixtures(Long tournamentId) {

        if (!tournamentRepository.existsById(tournamentId)) {
            throw new TournamentNotFoundException(
                    "Tournament not found with ID: " + tournamentId
            );
        }

        List<Fixture> fixtures =
                fixtureRepository.findByTournamentGroup_Tournament_Id(
                        tournamentId
                );

        return convertToResponses(fixtures);
    }

    private List<FixtureResponse> convertToResponses(
            List<Fixture> fixtures) {

        List<FixtureResponse> responses = new ArrayList<>();

        for (Fixture fixture : fixtures) {

            responses.add(new FixtureResponse(
                    fixture.getId(),
                    fixture.getTournamentGroup().getGroupName(),
                    fixture.getTeamA().getId(),
                    fixture.getTeamA().getTeamName(),
                    fixture.getTeamB().getId(),
                    fixture.getTeamB().getTeamName(),
                    fixture.getMatchDateTime(),
                    fixture.getCourtNumber(),
                    fixture.getStatus()
            ));
        }

        return responses;
    }
}
