
package com.tournament.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tournament.model.Team;
import com.tournament.service.TeamService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    // Register a new team
    @PostMapping
    public Team registerTeam(@Valid @RequestBody Team team) {
        return teamService.registerTeam(team);
    }

    // Retrieve all teams
    @GetMapping
    public List<Team> getAllTeams() {
        return teamService.getAllTeams();
    }
}
