
package com.tournament.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tournament.dto.TeamRequest;
import com.tournament.exception.DuplicateTeamException;
import com.tournament.exception.TeamLimitReachedException;
import com.tournament.exception.TeamNotFoundException;
import com.tournament.model.Team;
import com.tournament.repository.TeamRepository;
@Service
public class TeamService {

    private static final long MAX_TEAMS = 24;
    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    // Register a new team
    public Team registerTeam(TeamRequest request) {

        // Check whether the tournament is full
        if (teamRepository.count() >= MAX_TEAMS) {
            throw new TeamLimitReachedException(
                "Tournament is full. Maximum 24 teams allowed!"
            );
        }

        String teamName = request.getTeamName().trim();

        // Check for duplicate team name
        if (teamRepository.existsByTeamName(teamName)) {
            throw new DuplicateTeamException("Team name already exists!");
        }

        Team team = new Team();

        team.setTeamName(teamName);
        team.setPlayer1Name(request.getPlayer1Name().trim());
        team.setPlayer2Name(request.getPlayer2Name().trim());
        team.setPlayer3Name(request.getPlayer3Name().trim());
        team.setStatus("PENDING");

        return teamRepository.save(team);
    }



    // Retrieve all registered teams
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team updateTeamStatus(Long teamId, String status) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(
                    "Team not found with ID: " + teamId
                ));

        team.setStatus(status);

        return teamRepository.save(team);
    }

    public Team getTeamById(Long teamId) {

        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(
                        "Team not found with ID: " + teamId
                ));
    }

}

