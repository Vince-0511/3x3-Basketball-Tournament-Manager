
package com.tournament.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tournament.dto.StandingsResponse;
import com.tournament.dto.StandingsResponse.TeamStanding;
import com.tournament.exception.TournamentNotFoundException;
import com.tournament.model.Fixture;
import com.tournament.model.GroupTeam;
import com.tournament.model.Team;
import com.tournament.model.TournamentGroup;
import com.tournament.repository.FixtureRepository;
import com.tournament.repository.GroupTeamRepository;
import com.tournament.repository.TournamentGroupRepository;
import com.tournament.repository.TournamentRepository;

@Service
public class StandingsService {

    private final TournamentRepository tournamentRepository;
    private final TournamentGroupRepository tournamentGroupRepository;
    private final GroupTeamRepository groupTeamRepository;
    private final FixtureRepository fixtureRepository;

    public StandingsService(
            TournamentRepository tournamentRepository,
            TournamentGroupRepository tournamentGroupRepository,
            GroupTeamRepository groupTeamRepository,
            FixtureRepository fixtureRepository) {

        this.tournamentRepository = tournamentRepository;
        this.tournamentGroupRepository = tournamentGroupRepository;
        this.groupTeamRepository = groupTeamRepository;
        this.fixtureRepository = fixtureRepository;
    }

    @Transactional(readOnly = true)
    public List<StandingsResponse> getStandings(Long tournamentId) {

        if (!tournamentRepository.existsById(tournamentId)) {
            throw new TournamentNotFoundException(
                    "Tournament not found with ID: " + tournamentId
            );
        }

        List<TournamentGroup> groups =
                tournamentGroupRepository.findByTournament_Id(tournamentId);

        groups.sort(Comparator.comparing(
                TournamentGroup::getGroupName
        ));

        List<StandingsResponse> responses = new ArrayList<>();

        for (TournamentGroup group : groups) {

            List<GroupTeam> groupTeams =
                    groupTeamRepository.findByTournamentGroup_Id(group.getId());

            Map<Long, TeamStanding> standingsMap = new HashMap<>();

            for (GroupTeam groupTeam : groupTeams) {

                Team team = groupTeam.getTeam();

                standingsMap.put(
                        team.getId(),
                        new TeamStanding(
                                team.getId(),
                                team.getTeamName(),
                                team.getSeed(),
                                team.getCompetitionStatus()
                        )
                );
            }

            List<Fixture> fixtures =
                    fixtureRepository.findByTournamentGroup_Id(group.getId());

            // Calculate match statistics
            for (Fixture fixture : fixtures) {

                if (!"COMPLETED".equals(fixture.getResultStatus())) {
                    continue;
                }

                Integer scoreA = fixture.getTeamAScore();
                Integer scoreB = fixture.getTeamBScore();

                if (scoreA == null || scoreB == null) {
                    continue;
                }

                TeamStanding standingA =
                        standingsMap.get(fixture.getTeamA().getId());

                TeamStanding standingB =
                        standingsMap.get(fixture.getTeamB().getId());

                if (standingA == null || standingB == null) {
                    continue;
                }

                Team winner = fixture.getWinner();

                if (winner == null) {
                    continue;
                }

                boolean teamAWon =
                        winner.getId().equals(fixture.getTeamA().getId());

                boolean teamBWon =
                        winner.getId().equals(fixture.getTeamB().getId());

                if (!teamAWon && !teamBWon) {
                    continue;
                }

                String resultType = fixture.getResultType();

                boolean includeAInAverage = true;
                boolean includeBInAverage = true;

                // Default competition points for a loss
                int teamALossPoints = 1;
                int teamBLossPoints = 1;

                // FORFEIT AND NO_SHOW
                if ("FORFEIT".equals(resultType)
                        || "NO_SHOW".equals(resultType)) {

                    Team forfeitingTeam = fixture.getForfeitingTeam();

                    if (forfeitingTeam == null) {
                        continue;
                    }

                    if (forfeitingTeam.getId().equals(
                            fixture.getTeamA().getId())) {

                        includeBInAverage = false;

                        // Ordinary forfeit: zero competition points
                        if ("FORFEIT".equals(resultType)) {
                            teamALossPoints = 0;
                        }

                    } else if (forfeitingTeam.getId().equals(
                            fixture.getTeamB().getId())) {

                        includeAInAverage = false;

                        // Ordinary forfeit: zero competition points
                        if ("FORFEIT".equals(resultType)) {
                            teamBLossPoints = 0;
                        }

                    } else {
                        continue;
                    }
                }

                // DEFAULT
                else if ("DEFAULT".equals(resultType)) {

                    Boolean winnerKeepsScore =
                            fixture.getWinnerKeepsScore();

                    if (winnerKeepsScore == null) {
                        continue;
                    }

                    // Winner's score is excluded when
                    // the winning team chooses forfeit treatment.
                    if (teamAWon && !winnerKeepsScore) {
                        includeAInAverage = false;
                    }

                    if (teamBWon && !winnerKeepsScore) {
                        includeBInAverage = false;
                    }
                }

                // NORMAL RESULT
                else if (!"NORMAL".equals(resultType)) {
                    continue;
                }

                // Update Team A standings
                standingA.recordMatch(
                        scoreA,
                        scoreB,
                        teamAWon,
                        includeAInAverage,
                        teamALossPoints
                );

                // Update Team B standings
                standingB.recordMatch(
                        scoreB,
                        scoreA,
                        teamBWon,
                        includeBInAverage,
                        teamBLossPoints
                );
            }

            List<TeamStanding> standings =
                    new ArrayList<>(standingsMap.values());

            // First: active teams before disqualified teams.
            // Second: most wins.
            standings.sort(
                    Comparator.comparingInt(
                            (TeamStanding team) ->
                                    "DISQUALIFIED".equals(
                                            team.getCompetitionStatus()
                                    ) ? 1 : 0
                    )
                    .thenComparing(
                            Comparator.comparingInt(
                                    TeamStanding::getWon
                            ).reversed()
                    )
            );

            // Apply remaining FIBA tie-breakers.
            List<TeamStanding> finalStandings = new ArrayList<>();

            int index = 0;

            while (index < standings.size()) {

                TeamStanding first = standings.get(index);

                boolean disqualified =
                        "DISQUALIFIED".equals(
                                first.getCompetitionStatus()
                        );

                int wins = first.getWon();

                List<TeamStanding> tiedTeams = new ArrayList<>();

                while (index < standings.size()) {

                    TeamStanding current = standings.get(index);

                    boolean currentDisqualified =
                            "DISQUALIFIED".equals(
                                    current.getCompetitionStatus()
                            );

                    if (currentDisqualified != disqualified
                            || current.getWon() != wins) {
                        break;
                    }

                    tiedTeams.add(current);
                    index++;
                }

                if (!disqualified && tiedTeams.size() > 1) {

                    // Head-to-head wins within the tied teams.
                    Map<Long, Integer> headToHeadWins = new HashMap<>();

                    Set<Long> tiedTeamIds = new HashSet<>();

                    for (TeamStanding team : tiedTeams) {
                        tiedTeamIds.add(team.getTeamId());
                        headToHeadWins.put(team.getTeamId(), 0);
                    }

                    for (Fixture fixture : fixtures) {

                        if (!"COMPLETED".equals(
                                fixture.getResultStatus())) {
                            continue;
                        }

                        if (!tiedTeamIds.contains(
                                fixture.getTeamA().getId())
                                || !tiedTeamIds.contains(
                                        fixture.getTeamB().getId())) {
                            continue;
                        }

                        Team winner = fixture.getWinner();

                        if (winner != null) {
                            headToHeadWins.merge(
                                    winner.getId(),
                                    1,
                                    Integer::sum
                            );
                        }
                    }

                    tiedTeams.sort(
                            Comparator.comparingInt(
                                    (TeamStanding team) ->
                                            headToHeadWins.getOrDefault(
                                                    team.getTeamId(), 0
                                            )
                            ).reversed()
                            .thenComparing(
                                    Comparator.comparingDouble(
                                            TeamStanding::getAveragePointsScored
                                    ).reversed()
                            )
                            .thenComparing(
                                    TeamStanding::getSeed,
                                    Comparator.nullsLast(
                                            Comparator.naturalOrder()
                                    )
                            )
                            .thenComparing(TeamStanding::getTeamName)
                    );

                } else {

                    // Average points and seeding for remaining ties.
                    tiedTeams.sort(
                            Comparator.comparingDouble(
                                    TeamStanding::getAveragePointsScored
                            ).reversed()
                            .thenComparing(
                                    TeamStanding::getSeed,
                                    Comparator.nullsLast(
                                            Comparator.naturalOrder()
                                    )
                            )
                            .thenComparing(TeamStanding::getTeamName)
                    );
                }

                finalStandings.addAll(tiedTeams);
            }

            responses.add(
                    new StandingsResponse(
                            group.getGroupName(),
                            finalStandings
                    )
            );
        }

        return responses;
    }
}
