
package com.tournament.dto;

import java.time.LocalDateTime;

public class MatchResultResponse {

    private Long fixtureId;
    private Long teamAId;
    private Long teamBId;

    private String groupName;
    private String teamAName;
    private String teamBName;

    private Integer teamAScore;
    private Integer teamBScore;

    private String resultStatus;
    private String winnerName;
    private LocalDateTime matchDateTime;

    private String resultType;
    private String forfeitingTeamName;
    private Boolean winnerKeepsScore;

    public MatchResultResponse(
            Long fixtureId,
            Long teamAId,
            Long teamBId,
            String groupName,
            String teamAName,
            String teamBName,
            Integer teamAScore,
            Integer teamBScore,
            String resultStatus,
            String winnerName,
            LocalDateTime matchDateTime,
            String resultType,
            String forfeitingTeamName,
            Boolean winnerKeepsScore) {

        this.fixtureId = fixtureId;
        this.teamAId = teamAId;
        this.teamBId = teamBId;

        this.groupName = groupName;
        this.teamAName = teamAName;
        this.teamBName = teamBName;

        this.teamAScore = teamAScore;
        this.teamBScore = teamBScore;

        this.resultStatus = resultStatus;
        this.winnerName = winnerName;
        this.matchDateTime = matchDateTime;

        this.resultType = resultType;
        this.forfeitingTeamName = forfeitingTeamName;
        this.winnerKeepsScore = winnerKeepsScore;
    }

    public Long getFixtureId() {
        return fixtureId;
    }

    public Long getTeamAId() {
        return teamAId;
    }

    public Long getTeamBId() {
        return teamBId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getTeamAName() {
        return teamAName;
    }

    public String getTeamBName() {
        return teamBName;
    }

    public Integer getTeamAScore() {
        return teamAScore;
    }

    public Integer getTeamBScore() {
        return teamBScore;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public LocalDateTime getMatchDateTime() {
        return matchDateTime;
    }

    public String getResultType() {
        return resultType;
    }

    public String getForfeitingTeamName() {
        return forfeitingTeamName;
    }

    public Boolean getWinnerKeepsScore() {
        return winnerKeepsScore;
    }
}
