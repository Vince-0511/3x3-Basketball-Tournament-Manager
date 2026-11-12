
package com.tournament.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tournament.dto.GroupResponse;
import com.tournament.exception.GroupAllocationException;
import com.tournament.exception.TournamentNotFoundException;
import com.tournament.model.GroupTeam;
import com.tournament.model.Team;
import com.tournament.model.Tournament;
import com.tournament.model.TournamentGroup;
import com.tournament.repository.GroupTeamRepository;
import com.tournament.repository.TeamRepository;
import com.tournament.repository.TournamentGroupRepository;
import com.tournament.repository.TournamentRepository;

@Service
public class GroupService {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final TournamentGroupRepository tournamentGroupRepository;
    private final GroupTeamRepository groupTeamRepository;

    public GroupService(
            TournamentRepository tournamentRepository,
            TeamRepository teamRepository,
            TournamentGroupRepository tournamentGroupRepository,
            GroupTeamRepository groupTeamRepository) {

        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.tournamentGroupRepository = tournamentGroupRepository;
        this.groupTeamRepository = groupTeamRepository;
    }

    @Transactional
    public List<GroupResponse> generateGroups(Long tournamentId) {

        // Find the selected tournament
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() ->
                        new TournamentNotFoundException(
                                "Tournament not found with ID: " + tournamentId
                        )
                );

        // Prevent duplicate group generation
        if (tournamentGroupRepository.existsByTournament_Id(tournamentId)) {
            throw new GroupAllocationException(
                    "Groups have already been generated for this tournament"
            );
        }

        // Retrieve approved teams from this tournament only
        List<Team> approvedTeams =
                teamRepository.findByTournamentIdAndStatus(
                        tournamentId, "APPROVED"
                );

        // Validate that all required teams are approved
        if (approvedTeams.size() != tournament.getMaxTeams()) {
            throw new GroupAllocationException(
                    "This tournament requires exactly "
                    + tournament.getMaxTeams()
                    + " approved teams, but currently has "
                    + approvedTeams.size()
            );
        }

        // Prevent previously assigned seeds from being overwritten
        if (approvedTeams.stream()
                .anyMatch(team -> team.getSeed() != null)) {

            throw new GroupAllocationException(
                    "Team seeds have already been assigned for this tournament"
            );
        }

        // Determine group configuration
        int groupCount;
        int teamsPerGroup;

        if (tournament.getMaxTeams() == 24) {
            groupCount = 8;
            teamsPerGroup = 3;

        } else if (tournament.getMaxTeams() == 16) {
            groupCount = 4;
            teamsPerGroup = 4;

        } else if (tournament.getMaxTeams() == 20) {
            groupCount = 4;
            teamsPerGroup = 5;

        } else {
            throw new GroupAllocationException(
                    "Unsupported tournament capacity"
            );
        }

        // Create an independent list for random seeding
        List<Team> seedOrder = new ArrayList<>(approvedTeams);

        // Randomly assign seeds independently
        Collections.shuffle(seedOrder);

        for (int i = 0; i < seedOrder.size(); i++) {
            seedOrder.get(i).setSeed(i + 1);
        }

        // Save permanent seeds
        teamRepository.saveAll(seedOrder);

        // Independently shuffle teams for group allocation
        Collections.shuffle(approvedTeams);

        List<GroupResponse> responses = new ArrayList<>();

        int teamIndex = 0;

        // Generate tournament groups
        for (int i = 0; i < groupCount; i++) {

            String groupName = "Group " + (char) ('A' + i);

            TournamentGroup group = new TournamentGroup();
            group.setGroupName(groupName);
            group.setTournament(tournament);

            group = tournamentGroupRepository.save(group);

            List<GroupResponse.TeamInfo> teamInfos = new ArrayList<>();

            // Allocate teams into each group
            for (int j = 0; j < teamsPerGroup; j++) {

                Team team = approvedTeams.get(teamIndex++);

                // Prevent duplicate team assignment
                if (groupTeamRepository
                        .existsByTournamentGroup_Tournament_IdAndTeam_Id(
                                tournamentId, team.getId())) {

                    throw new GroupAllocationException(
                            "A team has already been assigned in this tournament"
                    );
                }

                GroupTeam groupTeam = new GroupTeam();
                groupTeam.setTournamentGroup(group);
                groupTeam.setTeam(team);

                groupTeamRepository.save(groupTeam);

                teamInfos.add(new GroupResponse.TeamInfo(
                        team.getId(),
                        team.getTeamName(),
                        team.getPlayer1Name(),
                        team.getPlayer2Name(),
                        team.getPlayer3Name()
                ));
            }

            responses.add(new GroupResponse(groupName, teamInfos));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getGroups(Long tournamentId) {

        // Verify that the tournament exists
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new TournamentNotFoundException(
                    "Tournament not found with ID: " + tournamentId
            );
        }

        List<TournamentGroup> groups =
                tournamentGroupRepository.findByTournament_Id(tournamentId);

        List<GroupResponse> responses = new ArrayList<>();

        for (TournamentGroup group : groups) {

            List<GroupTeam> groupTeams =
                    groupTeamRepository.findByTournamentGroup_Id(group.getId());

            List<GroupResponse.TeamInfo> teamInfos = new ArrayList<>();

            for (GroupTeam groupTeam : groupTeams) {

                Team team = groupTeam.getTeam();

                teamInfos.add(new GroupResponse.TeamInfo(
                        team.getId(),
                        team.getTeamName(),
                        team.getPlayer1Name(),
                        team.getPlayer2Name(),
                        team.getPlayer3Name()
                ));
            }

            responses.add(new GroupResponse(
                    group.getGroupName(),
                    teamInfos
            ));
        }

        return responses;
    }
}
