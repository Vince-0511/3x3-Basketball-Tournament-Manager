
package com.tournament.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tournament.dto.StandingsResponse;
import com.tournament.service.StandingsService;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}/standings")
@CrossOrigin(origins = "*")
public class StandingsController {

    private final StandingsService standingsService;

    public StandingsController(StandingsService standingsService) {
        this.standingsService = standingsService;
    }

    @GetMapping
    public ResponseEntity<List<StandingsResponse>> getStandings(
            @PathVariable Long tournamentId) {

        List<StandingsResponse> standings =
                standingsService.getStandings(tournamentId);

        return ResponseEntity.ok(standings);
    }
}

