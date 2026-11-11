
package com.tournament.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tournament.model.TournamentGroup;

public interface TournamentGroupRepository extends JpaRepository<TournamentGroup, Long> {

    boolean existsByTournament_Id(Long tournamentId);

    List<TournamentGroup> findByTournament_Id(Long tournamentId);
}
