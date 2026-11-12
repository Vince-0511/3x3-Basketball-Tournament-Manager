
package com.tournament.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tournament.dto.FixtureResponse;
import com.tournament.service.FixtureService;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}/fixtures")
@CrossOrigin(origins = "*")
public class FixtureController {

    private final FixtureService fixtureService;

    public FixtureController(FixtureService fixtureService) {
        this.fixtureService = fixtureService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<FixtureResponse>> generateFixtures(
            @PathVariable Long tournamentId) {

        List<FixtureResponse> fixtures =
                fixtureService.generateFixtures(tournamentId);

        return ResponseEntity.ok(fixtures);
    }

    @GetMapping
    public ResponseEntity<List<FixtureResponse>> getFixtures(
            @PathVariable Long tournamentId) {

        List<FixtureResponse> fixtures =
                fixtureService.getFixtures(tournamentId);

        return ResponseEntity.ok(fixtures);
    }
}
