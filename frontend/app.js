const API_URL = "http://localhost:8080/api/teams";
const TOURNAMENT_API_URL = "http://localhost:8080/api/tournaments";

// Load teams when the page opens

// Load dashboard data when the page opens
document.addEventListener("DOMContentLoaded", () => {
    loadTeams();
    loadTournaments();

    document.getElementById("tournamentForm")
        .addEventListener("submit", createTournament);

    document.getElementById("groupTournamentSelect")
        .addEventListener("change", loadGroups);

    document.getElementById("generateGroupsButton")
        .addEventListener("click", generateGroups);

    document.getElementById("refreshGroupsButton")
        .addEventListener("click", loadGroups);
});


// Get teams from the backend
async function loadTeams() {
    const tableBody = document.getElementById("teamTableBody");

    tableBody.innerHTML = `
        <tr>
            <td colspan="7">Loading teams...</td>
        </tr>
    `;

    try {
        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Failed to retrieve teams");
        }

        const teams = await response.json();

        displayTeams(teams);
        updateStatistics(teams);

    } catch (error) {
        console.error("Error loading teams:", error);

        tableBody.innerHTML = `
            <tr>
                <td colspan="7">
                    Unable to load teams. Please check your backend connection.
                </td>
            </tr>
        `;
    }
}

// Display teams in the table


function displayTeams(teams) {
    const tableBody = document.getElementById("teamTableBody");

    if (teams.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="7">No teams registered yet.</td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = teams.map(team => {
        let statusHTML = "";
        let actionsHTML = "";

        if (team.status === "PENDING") {
            statusHTML = `<span class="status-badge pending">Pending</span>`;

            actionsHTML = `
                <button onclick="approveTeam(${team.id})">Approve</button>
                <button onclick="rejectTeam(${team.id})">Reject</button>
                <button class="delete-button" onclick="deleteTeam(${team.id})">Delete</button>
            `;
        } else if (team.status === "APPROVED") {
            statusHTML = `<span class="status-badge approved">Approved</span>`;
            actionsHTML = `<span class="no-action">No action</span>`;
        } else if (team.status === "REJECTED") {
            statusHTML = `<span class="status-badge rejected">Rejected</span>`;

            actionsHTML = `
                <button class="delete-button" onclick="deleteTeam(${team.id})">Delete</button>
            `;
        }

        return `
            <tr>
                <td>${team.id}</td>
                <td>${escapeHTML(team.teamName)}</td>
                <td>${escapeHTML(team.player1Name)}</td>
                <td>${escapeHTML(team.player2Name)}</td>
                <td>${escapeHTML(team.player3Name)}</td>
                <td>${statusHTML}</td>
                <td>${actionsHTML}</td>
            </tr>
        `;
    }).join("");
}



// Update dashboard statistics
function updateStatistics(teams) {
    const total = teams.length;

    const pending = teams.filter(
        team => team.status === "PENDING"
    ).length;

    const approved = teams.filter(
        team => team.status === "APPROVED"
    ).length;

    const rejected = teams.filter(
        team => team.status === "REJECTED"
    ).length;

    document.getElementById("totalTeams").textContent = total;
    document.getElementById("pendingTeams").textContent = pending;
    document.getElementById("approvedTeams").textContent = approved;
    document.getElementById("rejectedTeams").textContent = rejected;
}

// Approve a team
async function approveTeam(teamId) {
    await updateTeamStatus(teamId, "APPROVED");
}

// Reject a team
async function rejectTeam(teamId) {
    await updateTeamStatus(teamId, "REJECTED");
}

// Send status update to backend
async function updateTeamStatus(teamId, status) {
    try {
        const response = await fetch(`${API_URL}/${teamId}/status`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ status: status })
        });

        if (!response.ok) {
            throw new Error("Failed to update team status");
        }

        alert(`Team ${status.toLowerCase()} successfully!`);

        // Refresh dashboard after updating
        loadTeams();

    } catch (error) {
        console.error("Error updating team:", error);
        alert("Unable to update team status.");
    }
}

// Escape user-submitted text before displaying it as HTML
function escapeHTML(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

// Refresh button
document.getElementById("refreshButton").addEventListener("click", loadTeams);


async function deleteTeam(teamId) {
    const confirmed = confirm(
        "Are you sure you want to delete this team? This action cannot be undone."
    );

    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(`${API_URL}/${teamId}`, {
            method: "DELETE"
        });

        if (!response.ok) {
            throw new Error("Failed to delete team");
        }

        alert("Team deleted successfully!");

        loadTeams();

    } catch (error) {
        console.error("Error deleting team:", error);
        alert("Unable to delete team.");
    }
}


// Load tournaments from the backend
async function loadTournaments() {
    const tournamentList = document.getElementById("tournamentList");

    tournamentList.innerHTML = "<p>Loading tournaments...</p>";

    try {
        const response = await fetch(TOURNAMENT_API_URL);

        if (!response.ok) {
            throw new Error("Failed to retrieve tournaments");
        }

        const tournaments = await response.json();

        displayTournaments(tournaments);
        populateGroupTournamentSelect(tournaments);

    } catch (error) {
        console.error("Error loading tournaments:", error);

        tournamentList.innerHTML = `
            <p>Unable to load tournaments. Please check your backend connection.</p>
        `;
    }
}


function populateGroupTournamentSelect(tournaments) {
    const select = document.getElementById("groupTournamentSelect");

    select.innerHTML = '<option value="">Select a tournament</option>';

    tournaments.forEach(tournament => {
        const option = document.createElement("option");

        option.value = tournament.id;
        option.textContent =
            `${tournament.tournamentName} (${tournament.maxTeams} teams)`;

        select.appendChild(option);
    });

    if (tournaments.length === 0) {
        select.innerHTML = '<option value="">No tournaments available</option>';
    }
}


// Display tournaments
function displayTournaments(tournaments) {
    const tournamentList = document.getElementById("tournamentList");

    if (tournaments.length === 0) {
        tournamentList.innerHTML = "<p>No tournaments created yet.</p>";
        return;
    }

    tournamentList.innerHTML = tournaments.map(tournament => `
        <div class="tournament-card">
            <h3>${escapeHTML(tournament.tournamentName)}</h3>

            <p><strong>Venue:</strong>
                ${escapeHTML(tournament.venue || "Not specified")}
            </p>

            <p><strong>Start Date:</strong>
                ${escapeHTML(tournament.startDate || "Not specified")}
            </p>

            <p><strong>End Date:</strong>
                ${escapeHTML(tournament.endDate || "Not specified")}
            </p>

            <p><strong>Maximum Teams:</strong>
                ${tournament.maxTeams}
            </p>

            <p><strong>Number of Courts:</strong>
                ${tournament.courtCount}
            </p>
        </div>
    `).join("");
}


// Create a tournament
async function createTournament(event) {
    event.preventDefault();

    const tournamentData = {
        tournamentName: document.getElementById("tournamentName").value.trim(),
        venue: document.getElementById("venue").value.trim() || null,
        startDate: document.getElementById("startDate").value || null,
        endDate: document.getElementById("endDate").value || null,
        maxTeams: Number(document.getElementById("maxTeams").value),
        courtCount: Number(document.getElementById("courtCount").value)
    };

    const message = document.getElementById("tournamentMessage");

    if (
        tournamentData.startDate &&
        tournamentData.endDate &&
        tournamentData.endDate < tournamentData.startDate
    ) {
        message.textContent = "End date cannot be before start date.";
        return;
    }

    try {
        const response = await fetch(TOURNAMENT_API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(tournamentData)
        });

        const result = await response.json();

        if (!response.ok) {
            const errorMessage = Object.values(result).join(", ");
            throw new Error(errorMessage || "Failed to create tournament");
        }

        message.textContent = "Tournament created successfully!";
        message.style.color = "green";

        document.getElementById("tournamentForm").reset();

        await loadTournaments();

    } catch (error) {
        console.error("Error creating tournament:", error);

        message.textContent = error.message || "Unable to create tournament.";
        message.style.color = "red";
    }
}


// Load groups for the selected tournament
async function loadGroups() {
    const tournamentId = document.getElementById("groupTournamentSelect").value;
    const groupList = document.getElementById("groupList");
    const groupMessage = document.getElementById("groupMessage");
    const generateButton = document.getElementById("generateGroupsButton");

    groupMessage.textContent = "";
    generateButton.disabled = true;

    if (!tournamentId) {
        groupList.innerHTML = "<p>Select a tournament to view its groups.</p>";
        return;
    }

    groupList.innerHTML = "<p>Loading groups...</p>";

    try {
        const response = await fetch(
            `${TOURNAMENT_API_URL}/${tournamentId}/groups`
        );

        if (!response.ok) {
            throw new Error("Failed to retrieve groups.");
        }

        const groups = await response.json();

        if (groups.length === 0) {
            groupList.innerHTML = "<p>No groups generated for this tournament yet.</p>";
            generateButton.disabled = false;
            return;
        }

        displayGroups(groups);

        groupMessage.textContent = "Groups have already been generated.";
        groupMessage.style.color = "green";

    } catch (error) {
        console.error("Error loading groups:", error);

        groupList.innerHTML = "<p>Unable to load groups. Please try again.</p>";
        groupMessage.textContent = error.message;
        groupMessage.style.color = "red";
    }
}


// Display generated groups
function displayGroups(groups) {
    const groupList = document.getElementById("groupList");

    groupList.innerHTML = groups.map(group => `
        <div class="group-card">
            <h3>${escapeHTML(group.groupName)}</h3>

            ${group.teams.map(team => `
                <div class="group-team">
                    <strong>${escapeHTML(team.teamName)}</strong>

                    <p>Player 1: ${escapeHTML(team.player1Name)}</p>
                    <p>Player 2: ${escapeHTML(team.player2Name)}</p>
                    <p>Player 3: ${escapeHTML(team.player3Name)}</p>
                </div>
            `).join("")}
        </div>
    `).join("");
}


// Generate groups for the selected tournament
async function generateGroups() {
    const tournamentId = document.getElementById("groupTournamentSelect").value;
    const groupMessage = document.getElementById("groupMessage");
    const generateButton = document.getElementById("generateGroupsButton");

    if (!tournamentId) {
        groupMessage.textContent = "Please select a tournament first.";
        groupMessage.style.color = "red";
        return;
    }

    const confirmed = confirm(
        "Are you sure you want to generate groups for this tournament? This action cannot be undone."
    );

    if (!confirmed) {
        return;
    }

    generateButton.disabled = true;
    groupMessage.textContent = "Generating groups...";
    groupMessage.style.color = "#64748b";

    try {
        const response = await fetch(
            `${TOURNAMENT_API_URL}/${tournamentId}/groups/generate`,
            {
                method: "POST"
            }
        );

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || "Failed to generate groups.");
        }

        groupMessage.textContent = "Groups generated successfully!";
        groupMessage.style.color = "green";

        displayGroups(result);

        // Keep generation disabled after successful allocation.
        generateButton.disabled = true;


    } catch (error) {
        console.error("Error generating groups:", error);

        // Reload saved groups to determine whether generation is still allowed.
        await loadGroups();

        // Display the original error after refreshing the groups.
        groupMessage.textContent = error.message;
        groupMessage.style.color = "red";
    }

}
