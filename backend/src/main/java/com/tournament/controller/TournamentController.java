
package com.tournament.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tournament.dto.TeamResponse;
import com.tournament.dto.TournamentRequest;
import com.tournament.model.Tournament;
import com.tournament.service.TeamService;
import com.tournament.service.TournamentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tournaments")
@CrossOrigin(origins = "*")
public class TournamentController {

    private final TournamentService tournamentService;
    private final TeamService teamService;

    public TournamentController(
            TournamentService tournamentService,
            TeamService teamService) {

        this.tournamentService = tournamentService;
        this.teamService = teamService;
    }

    // Create a tournament
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tournament createTournament(
            @Valid @RequestBody TournamentRequest request) {

        return tournamentService.createTournament(request);
    }

    // Get all tournaments
    @GetMapping
    public List<Tournament> getAllTournaments() {

        return tournamentService.getAllTournaments();
    }

    // Get a tournament by ID
    @GetMapping("/{id}")
    public Tournament getTournamentById(
            @PathVariable Long id) {

        return tournamentService.getTournamentById(id);
    }

    // Update an existing tournament
    @PutMapping("/{id}")
    public Tournament updateTournament(
            @PathVariable Long id,
            @Valid @RequestBody TournamentRequest request) {

        return tournamentService.updateTournament(id, request);
    }

    // Get teams belonging to a tournament
    @GetMapping("/{id}/teams")
    public List<TeamResponse> getTeamsByTournament(
            @PathVariable Long id) {

        return teamService.getTeamsByTournament(id);
    }
}
