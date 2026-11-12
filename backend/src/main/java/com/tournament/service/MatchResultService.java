
package com.tournament.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tournament.dto.MatchResultRequest;
import com.tournament.dto.MatchResultResponse;
import com.tournament.exception.FixtureGenerationException;
import com.tournament.model.Fixture;
import com.tournament.model.Team;
import com.tournament.repository.FixtureRepository;
import com.tournament.repository.TeamRepository;
import com.tournament.repository.TournamentRepository;

@Service
public class MatchResultService {

    private final FixtureRepository fixtureRepository;
    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;

    public MatchResultService(
            FixtureRepository fixtureRepository,
            TournamentRepository tournamentRepository,
            TeamRepository teamRepository) {

        this.fixtureRepository = fixtureRepository;
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
    }

    // Submit a match result
    @Transactional
    public MatchResultResponse submitResult(
            Long fixtureId,
            MatchResultRequest request) {

        Fixture fixture = findFixture(fixtureId);

        if (!"UPCOMING".equals(fixture.getStatus())) {
            throw new FixtureGenerationException(
                    "Only upcoming matches can receive results"
            );
        }

        if (!"NOT_SUBMITTED".equals(fixture.getResultStatus())) {
            throw new FixtureGenerationException(
                    "A result has already been submitted"
            );
        }

        Team teamA = fixture.getTeamA();
        Team teamB = fixture.getTeamB();

        if (!"ACTIVE".equals(teamA.getCompetitionStatus())
                || !"ACTIVE".equals(teamB.getCompetitionStatus())) {

            throw new FixtureGenerationException(
                    "Disqualified teams cannot participate in matches"
            );
        }

        Integer scoreA = request.getTeamAScore();
        Integer scoreB = request.getTeamBScore();

        if (scoreA == null || scoreB == null
                || scoreA < 0 || scoreB < 0) {

            throw new FixtureGenerationException(
                    "Valid non-negative scores are required"
            );
        }

        String resultType = request.getResultType();

        if (resultType == null) {
            throw new FixtureGenerationException(
                    "Result type is required"
            );
        }

        Team forfeitingTeam = null;
        Team winner;

        // NORMAL RESULT
        if ("NORMAL".equals(resultType)) {

            if (request.getForfeitingTeamId() != null
                    || request.getWinnerKeepsScore() != null) {

                throw new FixtureGenerationException(
                        "Normal results cannot contain forfeit information"
                );
            }

            if (scoreA.equals(scoreB)) {
                throw new FixtureGenerationException(
                        "A match cannot end in a tie"
                );
            }

            winner = scoreA > scoreB ? teamA : teamB;

            fixture.setTeamAScore(scoreA);
            fixture.setTeamBScore(scoreB);
            fixture.setWinner(winner);
            fixture.setResultType("NORMAL");
            fixture.setForfeitingTeam(null);
            fixture.setWinnerKeepsScore(null);
        }

        // FORFEIT RESULT
        else if ("FORFEIT".equals(resultType)) {

            forfeitingTeam = findForfeitingTeam(
                    fixture,
                    request.getForfeitingTeamId()
            );

            winner = getOpponent(fixture, forfeitingTeam);

            // FIBA W-0 representation.
            // Winner is stored separately.
            fixture.setTeamAScore(0);
            fixture.setTeamBScore(0);
            fixture.setWinner(winner);
            fixture.setResultType("FORFEIT");
            fixture.setForfeitingTeam(forfeitingTeam);
            fixture.setWinnerKeepsScore(false);
        }

        // NO-SHOW RESULT
        else if ("NO_SHOW".equals(resultType)) {

            forfeitingTeam = findForfeitingTeam(
                    fixture,
                    request.getForfeitingTeamId()
            );

            winner = getOpponent(fixture, forfeitingTeam);

            fixture.setTeamAScore(0);
            fixture.setTeamBScore(0);
            fixture.setWinner(winner);
            fixture.setResultType("NO_SHOW");
            fixture.setForfeitingTeam(forfeitingTeam);
            fixture.setWinnerKeepsScore(false);
        }

        // DEFAULT RESULT
        else if ("DEFAULT".equals(resultType)) {

            forfeitingTeam = findForfeitingTeam(
                    fixture,
                    request.getForfeitingTeamId()
            );

            winner = getOpponent(fixture, forfeitingTeam);

            if (request.getWinnerKeepsScore() == null) {
                throw new FixtureGenerationException(
                        "Please specify whether the winner keeps the score"
                );
            }

            // Determine the winner using the defaulting team,
            // NOT the entered score.
            if (winner.getId().equals(teamA.getId())) {

                fixture.setTeamAScore(
                        Boolean.TRUE.equals(request.getWinnerKeepsScore())
                                ? scoreA : 0
                );

                fixture.setTeamBScore(0);

            } else {

                fixture.setTeamAScore(0);

                fixture.setTeamBScore(
                        Boolean.TRUE.equals(request.getWinnerKeepsScore())
                                ? scoreB : 0
                );
            }

            fixture.setWinner(winner);
            fixture.setResultType("DEFAULT");
            fixture.setForfeitingTeam(forfeitingTeam);
            fixture.setWinnerKeepsScore(
                    request.getWinnerKeepsScore()
            );
        }

        else {
            throw new FixtureGenerationException(
                    "Unsupported result type: " + resultType
            );
        }

        fixture.setResultStatus("PENDING_VERIFICATION");
        fixture.setStatus("PENDING_VERIFICATION");

        Fixture savedFixture = fixtureRepository.save(fixture);

        return convertToResponse(savedFixture);
    }

    // Verify a submitted result
    @Transactional
    public MatchResultResponse verifyResult(Long fixtureId) {

        Fixture fixture = findFixture(fixtureId);

        if (!"PENDING_VERIFICATION".equals(
                fixture.getResultStatus())) {

            throw new FixtureGenerationException(
                    "Only pending results can be verified"
            );
        }

        String resultType = fixture.getResultType();

        Team forfeitingTeam = fixture.getForfeitingTeam();

        if ("FORFEIT".equals(resultType)) {

            if (forfeitingTeam == null) {
                throw new FixtureGenerationException(
                        "Forfeiting team information is missing"
                );
            }

            int updatedForfeitCount =
                    forfeitingTeam.getForfeitCount() + 1;

            forfeitingTeam.setForfeitCount(updatedForfeitCount);

            // Second ordinary forfeit means disqualification
            if (updatedForfeitCount >= 2) {
                forfeitingTeam.setCompetitionStatus("DISQUALIFIED");
            }

            teamRepository.save(forfeitingTeam);
        }

        else if ("NO_SHOW".equals(resultType)
                || "DEFAULT".equals(resultType)) {

            if (forfeitingTeam == null) {
                throw new FixtureGenerationException(
                        "Responsible team information is missing"
                );
            }

            // Immediate disqualification
            forfeitingTeam.setCompetitionStatus("DISQUALIFIED");

            teamRepository.save(forfeitingTeam);
        }

        else if (!"NORMAL".equals(resultType)) {

            throw new FixtureGenerationException(
                    "Unsupported result type during verification"
            );
        }

        fixture.setResultStatus("COMPLETED");
        fixture.setStatus("COMPLETED");

        Fixture savedFixture = fixtureRepository.save(fixture);

        return convertToResponse(savedFixture);
    }

    // Get tournament results
    @Transactional(readOnly = true)
    public List<MatchResultResponse> getResults(Long tournamentId) {

        if (!tournamentRepository.existsById(tournamentId)) {
            throw new FixtureGenerationException(
                    "Tournament not found with ID: " + tournamentId
            );
        }

        return fixtureRepository
                .findByTournamentGroup_Tournament_Id(tournamentId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Find fixture
    private Fixture findFixture(Long fixtureId) {

        return fixtureRepository.findById(fixtureId)
                .orElseThrow(() ->
                        new FixtureGenerationException(
                                "Fixture not found with ID: " + fixtureId
                        )
                );
    }

    // Find the team responsible for the result
    private Team findForfeitingTeam(
            Fixture fixture,
            Long teamId) {

        if (teamId == null) {
            throw new FixtureGenerationException(
                    "Responsible team ID is required"
            );
        }

        if (fixture.getTeamA().getId().equals(teamId)) {
            return fixture.getTeamA();
        }

        if (fixture.getTeamB().getId().equals(teamId)) {
            return fixture.getTeamB();
        }

        throw new FixtureGenerationException(
                "The responsible team must belong to this fixture"
        );
    }

    // Get the opponent of the responsible team
    private Team getOpponent(
            Fixture fixture,
            Team responsibleTeam) {

        if (responsibleTeam.getId().equals(
                fixture.getTeamA().getId())) {

            return fixture.getTeamB();
        }

        return fixture.getTeamA();
    }

    // Convert fixture into response DTO
    private MatchResultResponse convertToResponse(Fixture fixture) {

        return new MatchResultResponse(
                fixture.getId(),

                fixture.getTeamA().getId(),
                fixture.getTeamB().getId(),

                fixture.getTournamentGroup().getGroupName(),

                fixture.getTeamA().getTeamName(),
                fixture.getTeamB().getTeamName(),

                fixture.getTeamAScore(),
                fixture.getTeamBScore(),

                fixture.getResultStatus(),

                fixture.getWinner() == null
                        ? null
                        : fixture.getWinner().getTeamName(),

                fixture.getMatchDateTime(),

                fixture.getResultType(),

                fixture.getForfeitingTeam() == null
                        ? null
                        : fixture.getForfeitingTeam().getTeamName(),

                fixture.getWinnerKeepsScore()
        );
    }

}
