
package com.tournament.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tournament.model.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByTeamName(String teamName);

}
