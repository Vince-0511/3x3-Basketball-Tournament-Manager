
package com.tournament.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tournament.model.Tournament;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
}
