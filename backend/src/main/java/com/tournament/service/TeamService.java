
package com.tournament.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tournament.dto.TeamRequest;
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
    
public Team registerTeam(TeamRequest request) {

    String teamName = request.getTeamName().trim();

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
}

