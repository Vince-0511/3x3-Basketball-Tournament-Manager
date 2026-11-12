
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
        private Integer seed;
        private String competitionStatus;

        private int played;
        private int won;
        private int lost;

        private int pointsFor;
        private int pointsAgainst;
        private int pointDifference;
        private int competitionPoints;

        private int averagePointsTotal;
        private int averageGames;

        public TeamStanding(
                Long teamId,
                String teamName,
                Integer seed,
                String competitionStatus) {

            this.teamId = teamId;
            this.teamName = teamName;
            this.seed = seed;
            this.competitionStatus = competitionStatus;
        }

        public Long getTeamId() {
            return teamId;
        }

        public String getTeamName() {
            return teamName;
        }

        public Integer getSeed() {
            return seed;
        }

        public String getCompetitionStatus() {
            return competitionStatus;
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

        public int getAveragePointsTotal() {
            return averagePointsTotal;
        }

        public int getAverageGames() {
            return averageGames;
        }

        public double getAveragePointsScored() {
            if (averageGames == 0) {
                return 0.0;
            }

            return (double) averagePointsTotal / averageGames;
        }

        public void recordMatch(
                int scored,
                int conceded,
                boolean wonMatch,
                boolean includeInAverage,
                int lossPoints) {

            played++;

            pointsFor += scored;
            pointsAgainst += conceded;

            pointDifference = pointsFor - pointsAgainst;

            if (wonMatch) {
                won++;
                competitionPoints += 2;
            } else {
                lost++;
                competitionPoints += lossPoints;
            }

            if (includeInAverage) {
                // FIBA tie-break average is capped at 21 points
                averagePointsTotal += Math.min(scored, 21);
                averageGames++;
            }
        }
    }
}
