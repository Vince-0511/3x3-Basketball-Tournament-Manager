
package com.tournament.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Team name is required")
    @Column(nullable = false)
    private String teamName;

    @NotBlank(message = "Player 1 name is required")
    @Column(nullable = false)
    private String player1Name;

    @NotBlank(message = "Player 2 name is required")
    @Column(nullable = false)
    private String player2Name;

    @NotBlank(message = "Player 3 name is required")
    @Column(nullable = false)
    private String player3Name;

    private String status = "PENDING";

    // Tournament seeding
    @Column
    private Integer seed;

    // Number of forfeits recorded
    @Column(nullable = false)
    private Integer forfeitCount = 0;

    // Competition eligibility
    @Column(nullable = false)
    private String competitionStatus = "ACTIVE";

    // Tournament relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = true)
    private Tournament tournament;

    public Team() {
    }

    public Long getId() {
        return id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    public String getPlayer3Name() {
        return player3Name;
    }

    public void setPlayer3Name(String player3Name) {
        this.player3Name = player3Name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSeed() {
        return seed;
    }

    public void setSeed(Integer seed) {
        this.seed = seed;
    }

    public Integer getForfeitCount() {
        return forfeitCount;
    }

    public void setForfeitCount(Integer forfeitCount) {
        this.forfeitCount = forfeitCount;
    }

    public String getCompetitionStatus() {
        return competitionStatus;
    }

    public void setCompetitionStatus(String competitionStatus) {
        this.competitionStatus = competitionStatus;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }
}
