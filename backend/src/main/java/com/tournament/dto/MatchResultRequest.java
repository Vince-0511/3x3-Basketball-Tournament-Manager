
package com.tournament.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public class MatchResultRequest {

    @NotNull(message = "Team A score is required")
    @PositiveOrZero(message = "Team A score cannot be negative")
    private Integer teamAScore;

    @NotNull(message = "Team B score is required")
    @PositiveOrZero(message = "Team B score cannot be negative")
    private Integer teamBScore;

    @NotNull(message = "Result type is required")
    @Pattern(
        regexp = "NORMAL|FORFEIT|DEFAULT|NO_SHOW",
        message = "Result type must be NORMAL, FORFEIT, DEFAULT or NO_SHOW"
    )
    private String resultType = "NORMAL";

    private Long forfeitingTeamId;

    private Boolean winnerKeepsScore;

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

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public Long getForfeitingTeamId() {
        return forfeitingTeamId;
    }

    public void setForfeitingTeamId(Long forfeitingTeamId) {
        this.forfeitingTeamId = forfeitingTeamId;
    }

    public Boolean getWinnerKeepsScore() {
        return winnerKeepsScore;
    }

    public void setWinnerKeepsScore(Boolean winnerKeepsScore) {
        this.winnerKeepsScore = winnerKeepsScore;
    }
}
