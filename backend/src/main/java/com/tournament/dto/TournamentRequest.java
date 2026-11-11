
package com.tournament.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TournamentRequest {

    @NotBlank(message = "Tournament name is required")
    @Size(max = 100, message = "Tournament name cannot exceed 100 characters")
    private String tournamentName;

    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 150, message = "Venue cannot exceed 150 characters")
    private String venue;

    @NotNull(message = "Maximum teams is required")
    @Min(value = 16, message = "Minimum team capacity is 16")
    @Max(value = 24, message = "Maximum team capacity is 24")
    private Integer maxTeams;

    @NotNull(message = "Court count is required")
    @Min(value = 1, message = "At least one court is required")
    @Max(value = 20, message = "Court count cannot exceed 20")
    private Integer courtCount;

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Integer getMaxTeams() {
        return maxTeams;
    }

    public void setMaxTeams(Integer maxTeams) {
        this.maxTeams = maxTeams;
    }

    public Integer getCourtCount() {
        return courtCount;
    }

    public void setCourtCount(Integer courtCount) {
        this.courtCount = courtCount;
    }
}
