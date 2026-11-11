
package com.tournament.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tournament.dto.TournamentRequest;
import com.tournament.exception.TournamentNotFoundException;
import com.tournament.model.Tournament;
import com.tournament.repository.TournamentRepository;

@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;

    public TournamentService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    // Create a new tournament
    public Tournament createTournament(TournamentRequest request) {

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
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException(
                "End date cannot be before start date"
            );
        }

        Tournament tournament = new Tournament();

        tournament.setTournamentName(request.getTournamentName().trim());
        tournament.setStartDate(request.getStartDate());
        tournament.setEndDate(request.getEndDate());
        tournament.setVenue(
            request.getVenue() == null ? null : request.getVenue().trim()
        );
        tournament.setMaxTeams(request.getMaxTeams());
        tournament.setCourtCount(request.getCourtCount());

        return tournamentRepository.save(tournament);
    }

    // Retrieve all tournaments
    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    // Retrieve a tournament by ID
    public Tournament getTournamentById(Long id) {
        return tournamentRepository.findById(id)
            .orElseThrow(() -> new TournamentNotFoundException(
                "Tournament not found with ID: " + id
            ));
    }
}
