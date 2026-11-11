
package com.tournament.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tournament.model.GroupTeam;

public interface GroupTeamRepository extends JpaRepository<GroupTeam, Long> {

    boolean existsByTournamentGroup_Tournament_IdAndTeam_Id(
        Long tournamentId,
        Long teamId
    );

    List<GroupTeam> findByTournamentGroup_Id(Long groupId);
}
