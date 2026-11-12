
package com.tournament.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tournament.model.Fixture;

public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    boolean existsByTournamentGroup_Tournament_Id(Long tournamentId);

    List<Fixture> findByTournamentGroup_Tournament_Id(Long tournamentId);

    List<Fixture> findByTournamentGroup_Id(Long groupId);

}
