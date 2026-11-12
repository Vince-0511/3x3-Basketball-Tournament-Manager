
package com.tournament.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                        new TeamStanding(team.getId(), team.getTeamName())
                );
            }

            List<Fixture> fixtures =
                    fixtureRepository.findByTournamentGroup_Id(group.getId());

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

                standingA.recordMatch(scoreA, scoreB);
                standingB.recordMatch(scoreB, scoreA);
            }

            List<TeamStanding> standings =
                    new ArrayList<>(standingsMap.values());

            standings.sort(
                    Comparator.comparingInt(
                            TeamStanding::getCompetitionPoints
                    ).reversed()
                    .thenComparing(
                            Comparator.comparingInt(
                                    TeamStanding::getPointDifference
                            ).reversed()
                    )
                    .thenComparing(
                            Comparator.comparingInt(
                                    TeamStanding::getPointsFor
                            ).reversed()
                    )
                    .thenComparing(TeamStanding::getTeamName)
            );

            responses.add(
                    new StandingsResponse(group.getGroupName(), standings)
            );
        }

        return responses;
    }
}

