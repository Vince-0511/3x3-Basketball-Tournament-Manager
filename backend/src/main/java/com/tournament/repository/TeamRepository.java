
package com.tournament.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tournament.model.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByTeamName(String teamName);

    List<Team> findByStatus(String status);
}
