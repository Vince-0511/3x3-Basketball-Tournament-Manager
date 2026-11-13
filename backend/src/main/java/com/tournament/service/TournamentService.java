
package com.tournament.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tournament.dto.TournamentRequest;
import com.tournament.exception.TournamentNotFoundException;
import com.tournament.model.Tournament;
import com.tournament.repository.TeamRepository;
import com.tournament.repository.TournamentRepository;

@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;

    public TournamentService(
            TournamentRepository tournamentRepository,
            TeamRepository teamRepository) {

        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
    }

    // Create a new tournament
    public Tournament createTournament(TournamentRequest request) {

        validateTournamentRequest(request);

        Tournament tournament = new Tournament();

        applyTournamentDetails(tournament, request);

        return tournamentRepository.save(tournament);
    }

    // Update an existing tournament
    @Transactional
    public Tournament updateTournament(
            Long id,
            TournamentRequest request) {

        Tournament tournament = getTournamentById(id);

        validateTournamentRequest(request);

        // Count teams already registered in this tournament
        long registeredTeams =
                teamRepository.countByTournamentId(id);

        // Prevent capacity from being lower than registered teams
        if (request.getMaxTeams() < registeredTeams) {
            throw new IllegalArgumentException(
                    "Tournament capacity cannot be lower than the "
                    + "number of registered teams ("
                    + registeredTeams + ")"
            );
        }

        applyTournamentDetails(tournament, request);

        return tournamentRepository.save(tournament);
    }

    // Validate tournament information
    private void validateTournamentRequest(
            TournamentRequest request) {

        // Only allow 16, 20 or 24 teams
        if (request.getMaxTeams() != 16
                && request.getMaxTeams() != 20
                && request.getMaxTeams() != 24) {

            throw new IllegalArgumentException(
                    "Maximum teams must be 16, 20 or 24"
            );
        }

        // Validate tournament dates
        if (request.getStartDate() != null
                && request.getEndDate() != null
                && request.getEndDate().isBefore(
                        request.getStartDate())) {

            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }
    }

    // Apply tournament information
    private void applyTournamentDetails(
            Tournament tournament,
            TournamentRequest request) {

        tournament.setTournamentName(
                request.getTournamentName().trim()
        );

        tournament.setStartDate(request.getStartDate());
        tournament.setEndDate(request.getEndDate());

        tournament.setVenue(
                request.getVenue() == null
                        ? null
                        : request.getVenue().trim()
        );

        tournament.setMaxTeams(request.getMaxTeams());
        tournament.setCourtCount(request.getCourtCount());
    }

    // Retrieve all tournaments
    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    // Retrieve a tournament by ID
    public Tournament getTournamentById(Long id) {

        return tournamentRepository.findById(id)
                .orElseThrow(() ->
                        new TournamentNotFoundException(
                                "Tournament not found with ID: " + id
                        )
                );
    }
}
