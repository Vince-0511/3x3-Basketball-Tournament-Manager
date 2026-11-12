package com.tournament.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tournament.model.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {

    // Existing methods
    boolean existsByTeamName(String teamName);

    List<Team> findByStatus(String status);

    // Tournament-specific methods
    List<Team> findByTournamentId(Long tournamentId);

    long countByTournamentId(Long tournamentId);

    boolean existsByTournamentIdAndTeamName(
            Long tournamentId,
            String teamName
    );

    List<Team> findByTournamentIdAndStatus(
            Long tournamentId,
            String status
    );
}