
package com.tournament.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TeamRequest {

    @NotBlank(message = "Team name is required")
    @Size(max = 100, message = "Team name cannot exceed 100 characters")
    private String teamName;

    @NotBlank(message = "Player 1 name is required")
    @Size(max = 100, message = "Player 1 name cannot exceed 100 characters")
    private String player1Name;

    @NotBlank(message = "Player 2 name is required")
    @Size(max = 100, message = "Player 2 name cannot exceed 100 characters")
    private String player2Name;

    @NotBlank(message = "Player 3 name is required")
    @Size(max = 100, message = "Player 3 name cannot exceed 100 characters")
    private String player3Name;

    @NotNull(message = "Tournament ID is required")
    private Long tournamentId;

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

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }
}
