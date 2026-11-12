
package com.tournament.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tournament.dto.MatchResultRequest;
import com.tournament.dto.MatchResultResponse;
import com.tournament.service.MatchResultService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MatchResultController {

    private final MatchResultService matchResultService;

    public MatchResultController(
            MatchResultService matchResultService) {

        this.matchResultService = matchResultService;
    }

    @PostMapping("/fixtures/{fixtureId}/result")
    public ResponseEntity<MatchResultResponse> submitResult(
            @PathVariable Long fixtureId,
            @Valid @RequestBody MatchResultRequest request) {

        MatchResultResponse response =
                matchResultService.submitResult(fixtureId, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/fixtures/{fixtureId}/result/verify")
    public ResponseEntity<MatchResultResponse> verifyResult(
            @PathVariable Long fixtureId) {

        MatchResultResponse response =
                matchResultService.verifyResult(fixtureId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tournaments/{tournamentId}/results")
    public ResponseEntity<List<MatchResultResponse>> getResults(
            @PathVariable Long tournamentId) {

        List<MatchResultResponse> results =
                matchResultService.getResults(tournamentId);

        return ResponseEntity.ok(results);
    }
}
