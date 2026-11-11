
package com.tournament.dto;

import java.util.List;

public class GroupResponse {

    private String groupName;
    private List<TeamInfo> teams;

    public GroupResponse() {
    }

    public GroupResponse(String groupName, List<TeamInfo> teams) {
        this.groupName = groupName;
        this.teams = teams;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<TeamInfo> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamInfo> teams) {
        this.teams = teams;
    }

    public static class TeamInfo {

        private Long id;
        private String teamName;
        private String player1Name;
        private String player2Name;
        private String player3Name;

        public TeamInfo() {
        }

        public TeamInfo(
                Long id,
                String teamName,
                String player1Name,
                String player2Name,
                String player3Name) {

            this.id = id;
            this.teamName = teamName;
            this.player1Name = player1Name;
            this.player2Name = player2Name;
            this.player3Name = player3Name;
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
    }
}
