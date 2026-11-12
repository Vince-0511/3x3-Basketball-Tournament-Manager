
package com.tournament.dto;

import java.util.List;

public class StandingsResponse {

    private String groupName;
    private List<TeamStanding> standings;

    public StandingsResponse(
            String groupName,
            List<TeamStanding> standings) {

        this.groupName = groupName;
        this.standings = standings;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<TeamStanding> getStandings() {
        return standings;
    }

    public static class TeamStanding {

        private Long teamId;
        private String teamName;
        private int played;
        private int won;
        private int lost;
        private int pointsFor;
        private int pointsAgainst;
        private int pointDifference;
        private int competitionPoints;

        public TeamStanding(Long teamId, String teamName) {
            this.teamId = teamId;
            this.teamName = teamName;
        }

        public Long getTeamId() {
            return teamId;
        }

        public String getTeamName() {
            return teamName;
        }

        public int getPlayed() {
            return played;
        }

        public int getWon() {
            return won;
        }

        public int getLost() {
            return lost;
        }

        public int getPointsFor() {
            return pointsFor;
        }

        public int getPointsAgainst() {
            return pointsAgainst;
        }

        public int getPointDifference() {
            return pointDifference;
        }

        public int getCompetitionPoints() {
            return competitionPoints;
        }

        public void recordMatch(int scored, int conceded) {
            played++;
            pointsFor += scored;
            pointsAgainst += conceded;
            pointDifference = pointsFor - pointsAgainst;

            if (scored > conceded) {
                won++;
                competitionPoints += 2;
            } else {
                lost++;
                competitionPoints += 1;
            }
        }
    }
}