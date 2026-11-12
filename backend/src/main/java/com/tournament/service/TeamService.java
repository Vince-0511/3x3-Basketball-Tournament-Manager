
package com.tournament.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tournament.dto.TeamRequest;
import com.tournament.dto.TeamResponse;
import com.tournament.exception.DuplicateTeamException;
import com.tournament.exception.TeamCannotBeDeletedException;
import com.tournament.exception.TeamLimitReachedException;
import com.tournament.exception.TeamNotFoundException;
import com.tournament.exception.TournamentNotFoundException;
import com.tournament.model.Team;
import com.tournament.model.Tournament;
import com.tournament.repository.TeamRepository;
import com.tournament.repository.TournamentRepository;    

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;

    public TeamService(
            TeamRepository teamRepository,
            TournamentRepository tournamentRepository) {

        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
    }

    // Register a new team
    @Transactional
    public TeamResponse registerTeam(TeamRequest request) {

        // Find the selected tournament
        Tournament tournament = tournamentRepository
                .findById(request.getTournamentId())
                .orElseThrow(() -> new TournamentNotFoundException(
                        "Tournament not found with ID: "
                                + request.getTournamentId()));

        // Check capacity for this tournament only
        long registeredTeams = teamRepository
                .countByTournamentId(tournament.getId());

        if (registeredTeams >= tournament.getMaxTeams()) {
            throw new TeamLimitReachedException(
                    "Tournament is full. Maximum "
                            + tournament.getMaxTeams()
                            + " teams allowed!");
        }

        // Check duplicate names within this tournament only
        String teamName = request.getTeamName().trim();

        if (teamRepository.existsByTournamentIdAndTeamName(
                tournament.getId(), teamName)) {

            throw new DuplicateTeamException(
                    "Team name already exists in this tournament!");
        }

        // Create the new team
        Team team = new Team();

        team.setTournament(tournament);
        team.setTeamName(teamName);
        team.setPlayer1Name(request.getPlayer1Name().trim());
        team.setPlayer2Name(request.getPlayer2Name().trim());
        team.setPlayer3Name(request.getPlayer3Name().trim());
        team.setStatus("PENDING");

        // Save and return a safe DTO
        return new TeamResponse(teamRepository.save(team));
    }


    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll()
                .stream()
                .map(TeamResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByTournament(Long tournamentId) {

        // Verify that the tournament exists
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new TournamentNotFoundException(
                    "Tournament not found with ID: " + tournamentId);
        }

        // Retrieve teams belonging to this tournament only
        return teamRepository.findByTournamentId(tournamentId)
                .stream()
                .map(TeamResponse::new)
                .toList();
    }

    @Transactional
    public TeamResponse updateTeamStatus(Long teamId, String status) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(
                    "Team not found with ID: " + teamId
                ));

        team.setStatus(status);

        return new TeamResponse(teamRepository.save(team));
    }
    @Transactional(readOnly = true)
    public TeamResponse getTeamById(Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(
                        "Team not found with ID: " + teamId
                ));

        return new TeamResponse(team);
    }

    @Transactional 
    public void deleteTeam(Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(
                        "Team not found with ID: " + teamId
                ));

        if ("APPROVED".equals(team.getStatus())) {
            throw new TeamCannotBeDeletedException(
                    "Approved teams cannot be deleted!"
            );
        }

        teamRepository.delete(team);
    }

}

