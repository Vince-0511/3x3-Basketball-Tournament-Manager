
package com.tournament.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tournament.exception.DuplicateTeamException;
import com.tournament.model.Team;
import com.tournament.repository.TeamRepository;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    // Register a new team
    public Team registerTeam(Team team) {

        // Check for duplicate team name
        if (teamRepository.existsByTeamName(team.getTeamName().trim())) {
            throw new DuplicateTeamException("Team name already exists!");
        }

        // Remove unnecessary spaces
        team.setTeamName(team.getTeamName().trim());

        // Set initial registration status
        team.setStatus("PENDING");

        // Save team to database
        return teamRepository.save(team);
    }

    // Retrieve all registered teams
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }
}

