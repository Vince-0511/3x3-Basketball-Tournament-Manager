
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
import com.tournament.repository.TournamentRepository;

@Service
public class MatchResultService {

    private final FixtureRepository fixtureRepository;
    private final TournamentRepository tournamentRepository;

    public MatchResultService(
            FixtureRepository fixtureRepository,
            TournamentRepository tournamentRepository) {

        this.fixtureRepository = fixtureRepository;
        this.tournamentRepository = tournamentRepository;
    }

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
                    "A result has already been submitted for this match"
            );
        }

        Integer teamAScore = request.getTeamAScore();
        Integer teamBScore = request.getTeamBScore();

        if (teamAScore.equals(teamBScore)) {
            throw new FixtureGenerationException(
                    "A match cannot end in a tie"
            );
        }

        Team winner = teamAScore > teamBScore
                ? fixture.getTeamA()
                : fixture.getTeamB();

        fixture.setTeamAScore(teamAScore);
        fixture.setTeamBScore(teamBScore);
        fixture.setWinner(winner);
        fixture.setResultStatus("PENDING_VERIFICATION");
        fixture.setStatus("PENDING_VERIFICATION");

        Fixture savedFixture = fixtureRepository.save(fixture);

        return convertToResponse(savedFixture);
    }

    @Transactional
    public MatchResultResponse verifyResult(Long fixtureId) {

        Fixture fixture = findFixture(fixtureId);

        if (!"PENDING_VERIFICATION".equals(
                fixture.getResultStatus())) {

            throw new FixtureGenerationException(
                    "Only pending results can be verified"
            );
        }

        fixture.setResultStatus("COMPLETED");
        fixture.setStatus("COMPLETED");

        Fixture savedFixture = fixtureRepository.save(fixture);

        return convertToResponse(savedFixture);
    }

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

    private Fixture findFixture(Long fixtureId) {

        return fixtureRepository.findById(fixtureId)
                .orElseThrow(() ->
                        new FixtureGenerationException(
                                "Fixture not found with ID: " + fixtureId
                        )
                );
    }

    private MatchResultResponse convertToResponse(Fixture fixture) {

        return new MatchResultResponse(
                fixture.getId(),
                fixture.getTournamentGroup().getGroupName(),
                fixture.getTeamA().getTeamName(),
                fixture.getTeamB().getTeamName(),
                fixture.getTeamAScore(),
                fixture.getTeamBScore(),
                fixture.getResultStatus(),
                fixture.getWinner() == null
                        ? null
                        : fixture.getWinner().getTeamName(),
                fixture.getMatchDateTime()
        );
    }
}
