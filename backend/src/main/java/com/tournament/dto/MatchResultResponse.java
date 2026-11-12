
package com.tournament.dto;

import java.time.LocalDateTime;

public class MatchResultResponse {

    private Long fixtureId;
    private String groupName;
    private String teamAName;
    private String teamBName;
    private Integer teamAScore;
    private Integer teamBScore;
    private String resultStatus;
    private String winnerName;
    private LocalDateTime matchDateTime;

    public MatchResultResponse(
            Long fixtureId,
            String groupName,
            String teamAName,
            String teamBName,
            Integer teamAScore,
            Integer teamBScore,
            String resultStatus,
            String winnerName,
            LocalDateTime matchDateTime) {

        this.fixtureId = fixtureId;
        this.groupName = groupName;
        this.teamAName = teamAName;
        this.teamBName = teamBName;
        this.teamAScore = teamAScore;
        this.teamBScore = teamBScore;
        this.resultStatus = resultStatus;
        this.winnerName = winnerName;
        this.matchDateTime = matchDateTime;
    }

    public Long getFixtureId() {
        return fixtureId;
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
}
