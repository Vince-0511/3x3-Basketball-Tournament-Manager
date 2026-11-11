
package com.tournament.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "group_teams",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"group_id", "team_id"}
        )
    }
)
public class GroupTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private TournamentGroup tournamentGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    public GroupTeam() {
    }

    public Long getId() {
        return id;
    }

    public TournamentGroup getTournamentGroup() {
        return tournamentGroup;
    }

    public void setTournamentGroup(TournamentGroup tournamentGroup) {
        this.tournamentGroup = tournamentGroup;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
