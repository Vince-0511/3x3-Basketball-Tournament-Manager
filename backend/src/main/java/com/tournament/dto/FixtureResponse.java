
package com.tournament.dto;

import java.time.LocalDateTime;

public class FixtureResponse {

    private Long id;
    private String groupName;
    private Long teamAId;
    private String teamAName;
    private Long teamBId;
    private String teamBName;
    private LocalDateTime matchDateTime;
    private LocalDateTime matchEndDateTime;
    private Integer courtNumber;
    private String status;

    public FixtureResponse() {
    }

    public FixtureResponse(
            Long id,
            String groupName,
            Long teamAId,
            String teamAName,
            Long teamBId,
            String teamBName,
            LocalDateTime matchDateTime,
            Integer courtNumber,
            String status) {

        this.id = id;
        this.groupName = groupName;
        this.teamAId = teamAId;
        this.teamAName = teamAName;
        this.teamBId = teamBId;
        this.teamBName = teamBName;
        this.matchDateTime = matchDateTime;
        this.matchEndDateTime = matchDateTime.plusMinutes(20);
        this.courtNumber = courtNumber;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public Long getTeamAId() {
        return teamAId;
    }

    public String getTeamAName() {
        return teamAName;
    }

    public Long getTeamBId() {
        return teamBId;
    }

    public String getTeamBName() {
        return teamBName;
    }

    public LocalDateTime getMatchDateTime() {
        return matchDateTime;
    }

    public LocalDateTime getMatchEndDateTime() {
        return matchEndDateTime;
    }

    public Integer getCourtNumber() {
        return courtNumber;
    }

    public String getStatus() {
        return status;
    }
}
