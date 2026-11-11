
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

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() ->
                        new TournamentNotFoundException(
                                "Tournament not found with ID: " + tournamentId
                        )
                );

        if (tournamentGroupRepository.existsByTournament_Id(tournamentId)) {
            throw new GroupAllocationException(
                    "Groups have already been generated for this tournament"
            );
        }

        List<Team> approvedTeams = teamRepository.findByStatus("APPROVED");

        if (approvedTeams.size() != tournament.getMaxTeams()) {
            throw new GroupAllocationException(
                    "This tournament requires exactly "
                    + tournament.getMaxTeams()
                    + " approved teams, but currently has "
                    + approvedTeams.size()
            );
        }

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

        Collections.shuffle(approvedTeams);

        List<GroupResponse> responses = new ArrayList<>();

        int teamIndex = 0;

        for (int i = 0; i < groupCount; i++) {

            String groupName = "Group " + (char) ('A' + i);

            TournamentGroup group = new TournamentGroup();
            group.setGroupName(groupName);
            group.setTournament(tournament);

            group = tournamentGroupRepository.save(group);

            List<GroupResponse.TeamInfo> teamInfos = new ArrayList<>();

            for (int j = 0; j < teamsPerGroup; j++) {

                Team team = approvedTeams.get(teamIndex++);

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
