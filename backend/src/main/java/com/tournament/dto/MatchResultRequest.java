
package com.tournament.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class MatchResultRequest {

    @NotNull(message = "Team A score is required")
    @PositiveOrZero(message = "Team A score cannot be negative")
    private Integer teamAScore;

    @NotNull(message = "Team B score is required")
    @PositiveOrZero(message = "Team B score cannot be negative")
    private Integer teamBScore;

    public Integer getTeamAScore() {
        return teamAScore;
    }

    public void setTeamAScore(Integer teamAScore) {
        this.teamAScore = teamAScore;
    }

    public Integer getTeamBScore() {
        return teamBScore;
    }

    public void setTeamBScore(Integer teamBScore) {
        this.teamBScore = teamBScore;
    }
}
