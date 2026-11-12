package com.tournament.dto;

import com.tournament.model.Team;

public class TeamResponse {

    private Long id;
    private String teamName;
    private String player1Name;
    private String player2Name;
    private String player3Name;
    private String status;
    private Long tournamentId;

    public TeamResponse(Team team) {
        this.id = team.getId();
        this.teamName = team.getTeamName();
        this.player1Name = team.getPlayer1Name();
        this.player2Name = team.getPlayer2Name();
        this.player3Name = team.getPlayer3Name();
        this.status = team.getStatus();

        this.tournamentId = team.getTournament() != null
                ? team.getTournament().getId()
                : null;
    }

    public Long getId() {
        return id;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public String getPlayer3Name() {
        return player3Name;
    }

    public String getStatus() {
        return status;
    }

    public Long getTournamentId() {
        return tournamentId;
    }
}