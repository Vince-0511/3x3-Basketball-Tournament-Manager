const API_URL = "http://localhost:8080/api/teams";
const TOURNAMENT_API_URL = "http://localhost:8080/api/tournaments";
let editingTournamentId = null;
// Load teams when the page opens

// Load dashboard data when the page opens
document.addEventListener("DOMContentLoaded", () => {
    loadTeams();
    loadTournaments();
    initializeNavigation();

    document.getElementById("tournamentForm")
        .addEventListener("submit", createTournament);

    document.getElementById("cancelTournamentEditButton")
        .addEventListener("click", cancelTournamentEdit);

    document.getElementById("groupTournamentSelect")
        .addEventListener("change", loadGroups);

    document.getElementById("generateGroupsButton")
        .addEventListener("click", generateGroups);

    document.getElementById("refreshGroupsButton")
        .addEventListener("click", loadGroups);

        document.getElementById("fixtureTournamentSelect")
        .addEventListener("change", loadFixtures);

    document.getElementById("generateFixturesButton")
        .addEventListener("click", generateFixtures);

    document.getElementById("refreshFixturesButton")
        .addEventListener("click", loadFixtures);

    // Group Standings
    document.getElementById("standingsTournamentSelect")
        .addEventListener("change", loadStandings);

    document.getElementById("refreshStandingsButton")
        .addEventListener("click", loadStandings);

    // Match Results Management
    document.getElementById("resultsTournamentSelect")
        .addEventListener("change", loadResults);

    document.getElementById("refreshResultsButton")
        .addEventListener("click", loadResults);

    // Handle dynamically generated result action buttons
    document.getElementById("resultsTableBody")
        .addEventListener("click", async (event) => {

            const button = event.target.closest("button[data-action]");

            if (!button) {
                return;
            }

            const fixtureId = Number(button.dataset.fixtureId);
            const action = button.dataset.action;

            if (!Number.isSafeInteger(fixtureId) || fixtureId <= 0) {
                return;
            }

            if (action === "submit") {
                await submitMatchResult(fixtureId);
            } else if (action === "verify") {
                await verifyMatchResult(fixtureId);
            }
        });

    document.getElementById("resultsTableBody")
        .addEventListener("change", (event) => {

            if (event.target.matches(".result-type-select")) {
                toggleResultFields(event.target);
            }
        });

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
        populateFixtureTournamentSelect(tournaments);
        populateStandingsTournamentSelect(tournaments);
        populateResultsTournamentSelect(tournaments);

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


function populateFixtureTournamentSelect(tournaments) {
    const select = document.getElementById("fixtureTournamentSelect");

    select.innerHTML = '<option value="">Select a tournament</option>';

    tournaments.forEach(tournament => {
        const option = document.createElement("option");

        option.value = tournament.id;
        option.textContent =
            `${tournament.tournamentName} (${tournament.maxTeams} teams)`;

        select.appendChild(option);
    });

    if (tournaments.length === 0) {
        select.innerHTML =
            '<option value="">No tournaments available</option>';
    }
}


// Display tournaments

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

            <button
                type="button"
                onclick="editTournament(${Number(tournament.id)})">
                Edit Tournament
            </button>
        </div>
    `).join("");
}


// Edit an existing tournament
async function editTournament(tournamentId) {
    const message = document.getElementById("tournamentMessage");

    try {
        const response = await fetch(
            `${TOURNAMENT_API_URL}/${tournamentId}`
        );

        if (!response.ok) {
            throw new Error("Failed to retrieve tournament details.");
        }

        const tournament = await response.json();

        editingTournamentId = tournament.id;

        document.getElementById("tournamentName").value =
            tournament.tournamentName;

        document.getElementById("venue").value =
            tournament.venue || "";

        document.getElementById("startDate").value =
            tournament.startDate || "";

        document.getElementById("endDate").value =
            tournament.endDate || "";

        document.getElementById("maxTeams").value =
            tournament.maxTeams;

        document.getElementById("courtCount").value =
            tournament.courtCount;

        document.getElementById("tournamentFormTitle").textContent =
            "Edit Tournament";

        document.getElementById("tournamentSubmitButton").textContent =
            "Save Changes";

        document.getElementById("cancelTournamentEditButton").hidden =
            false;

        message.textContent =
            `Editing tournament: ${tournament.tournamentName}`;

        message.style.color = "#2563eb";

        document.getElementById("tournamentForm").scrollIntoView({
            behavior: "smooth",
            block: "center"
        });

    } catch (error) {
        console.error("Error loading tournament:", error);
        message.textContent = error.message;
        message.style.color = "red";
    }
}


// Cancel tournament editing
function cancelTournamentEdit() {
    editingTournamentId = null;

    document.getElementById("tournamentForm").reset();

    document.getElementById("tournamentFormTitle").textContent =
        "Create New Tournament";

    document.getElementById("tournamentSubmitButton").textContent =
        "Create Tournament";

    document.getElementById("cancelTournamentEditButton").hidden =
        true;

    const message = document.getElementById("tournamentMessage");

    message.textContent = "";
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

    // Validate tournament dates
    if (
        tournamentData.startDate &&
        tournamentData.endDate &&
        tournamentData.endDate < tournamentData.startDate
    ) {
        message.textContent = "End date cannot be before start date.";
        message.style.color = "red";
        return;
    }

    // Determine whether creating or updating
    const isEditing = editingTournamentId !== null;

    const url = isEditing
        ? `${TOURNAMENT_API_URL}/${editingTournamentId}`
        : TOURNAMENT_API_URL;

    const method = isEditing ? "PUT" : "POST";

    try {
        const response = await fetch(url, {
            method: method,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(tournamentData)
        });

        const result = await response.json();

        if (!response.ok) {
            const errorMessage =
                result.error || Object.values(result).join(", ");

            throw new Error(
                errorMessage || "Failed to save tournament."
            );
        }

        // Display success message
        message.textContent = isEditing
            ? "Tournament updated successfully!"
            : "Tournament created successfully!";

        message.style.color = "green";

        // Reset editing state
        editingTournamentId = null;

        document.getElementById("tournamentForm").reset();

        document.getElementById("tournamentFormTitle").textContent =
            "Create New Tournament";

        document.getElementById("tournamentSubmitButton").textContent =
            "Create Tournament";

        document.getElementById("cancelTournamentEditButton").hidden =
            true;

        // Refresh tournament list
        await loadTournaments();

    } catch (error) {
        console.error("Error saving tournament:", error);

        message.textContent =
            error.message || "Unable to save tournament.";

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


async function loadFixtures() {
    const tournamentId =
        document.getElementById("fixtureTournamentSelect").value;

    const tableBody = document.getElementById("fixtureTableBody");
    const message = document.getElementById("fixtureMessage");
    const generateButton =
        document.getElementById("generateFixturesButton");

    message.textContent = "";
    generateButton.disabled = true;

    if (!tournamentId) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="7">Select a tournament to view fixtures.</td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = `
        <tr>
            <td colspan="7">Loading fixtures...</td>
        </tr>
    `;

    try {
        const response = await fetch(
            `${TOURNAMENT_API_URL}/${tournamentId}/fixtures`
        );

        if (!response.ok) {
            throw new Error("Failed to retrieve fixtures.");
        }

        const fixtures = await response.json();

        if (fixtures.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="7">No fixtures generated yet.</td>
                </tr>
            `;

            message.textContent = "Ready to generate fixtures.";
            message.style.color = "#64748b";
            generateButton.disabled = false;
            return;
        }

        displayFixtures(fixtures);

        message.textContent =
            `${fixtures.length} fixtures have been generated.`;

        message.style.color = "green";
        generateButton.disabled = true;

    } catch (error) {
        console.error("Error loading fixtures:", error);

        tableBody.innerHTML = `
            <tr>
                <td colspan="7">Unable to load fixtures.</td>
            </tr>
        `;

        message.textContent = error.message;
        message.style.color = "red";
    }
}


function displayFixtures(fixtures) {
    const tableBody = document.getElementById("fixtureTableBody");

    if (fixtures.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="7">No fixtures available.</td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = fixtures.map(fixture => {
        const matchDate = new Date(fixture.matchDateTime);

        const formattedDate = matchDate.toLocaleDateString();
        const formattedTime = matchDate.toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit"
        });

        return `
            <tr>
                <td>${fixture.id}</td>
                <td>${escapeHTML(fixture.groupName)}</td>
                <td>${escapeHTML(fixture.teamAName)}</td>
                <td>${escapeHTML(fixture.teamBName)}</td>
                <td>${formattedDate} ${formattedTime}</td>
                <td>Court ${fixture.courtNumber}</td>
                <td>
                    <span class="status-badge pending">
                        ${escapeHTML(fixture.status)}
                    </span>
                </td>
            </tr>
        `;
    }).join("");
}


async function generateFixtures() {
    const tournamentId =
        document.getElementById("fixtureTournamentSelect").value;

    const message = document.getElementById("fixtureMessage");
    const generateButton =
        document.getElementById("generateFixturesButton");

    if (!tournamentId) {
        message.textContent = "Please select a tournament first.";
        message.style.color = "red";
        return;
    }

    const confirmed = confirm(
        "Are you sure you want to generate fixtures? This action cannot be undone."
    );

    if (!confirmed) {
        return;
    }

    generateButton.disabled = true;
    message.textContent = "Generating fixtures...";
    message.style.color = "#64748b";

    try {
        const response = await fetch(
            `${TOURNAMENT_API_URL}/${tournamentId}/fixtures/generate`,
            {
                method: "POST"
            }
        );

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || "Failed to generate fixtures.");
        }

        displayFixtures(result);

        message.textContent =
            `Successfully generated ${result.length} fixtures!`;

        message.style.color = "green";

        generateButton.disabled = true;

    } catch (error) {
        console.error("Error generating fixtures:", error);

        await loadFixtures();

        message.textContent = error.message;
        message.style.color = "red";
    }
}

// Populate tournament dropdown for Group Standings
function populateStandingsTournamentSelect(tournaments) {
    const select = document.getElementById("standingsTournamentSelect");

    select.innerHTML = '<option value="">Select a tournament</option>';

    tournaments.forEach(tournament => {
        const option = document.createElement("option");

        option.value = tournament.id;
        option.textContent =
            `${tournament.tournamentName} (${tournament.maxTeams} teams)`;

        select.appendChild(option);
    });

    if (tournaments.length === 0) {
        select.innerHTML =
            '<option value="">No tournaments available</option>';
    }
}

// Load group standings for the selected tournament
async function loadStandings() {
    const tournamentId =
        document.getElementById("standingsTournamentSelect").value;

    const standingsList =
        document.getElementById("standingsList");

    const message =
        document.getElementById("standingsMessage");

    message.textContent = "";

    if (!tournamentId) {
        standingsList.innerHTML =
            "<p>Select a tournament to view group standings.</p>";
        return;
    }

    standingsList.innerHTML = "<p>Loading standings...</p>";

    try {
        const response = await fetch(
            `${TOURNAMENT_API_URL}/${tournamentId}/standings`
        );

        if (!response.ok) {
            throw new Error("Failed to retrieve group standings.");
        }

        const groups = await response.json();

        if (groups.length === 0) {
            standingsList.innerHTML =
                "<p>No groups have been generated for this tournament.</p>";
            return;
        }

        displayStandings(groups);

        message.textContent =
            `Standings loaded for ${groups.length} groups.`;

        message.style.color = "green";

    } catch (error) {
        console.error("Error loading standings:", error);

        standingsList.innerHTML =
            "<p>Unable to load standings. Please try again.</p>";

        message.textContent = error.message;
        message.style.color = "red";
    }
}

// Display group standings in tables
function displayStandings(groups) {
    const standingsList =
        document.getElementById("standingsList");

    standingsList.innerHTML = groups.map(group => `
        <div class="standings-card">
            <h3>${escapeHTML(group.groupName)}</h3>

            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Rank</th>
                            <th>Team</th>
                            <th>P</th>
                            <th>W</th>
                            <th>L</th>
                            <th>PF</th>
                            <th>PA</th>
                            <th>PD</th>
                            <th>Pts</th>
                        </tr>
                    </thead>

                    <tbody>
                        ${group.standings.map((team, index) => `
                            <tr>
                                <td>${index + 1}</td>
                                <td>${escapeHTML(team.teamName)}</td>
                                <td>${team.played}</td>
                                <td>${team.won}</td>
                                <td>${team.lost}</td>
                                <td>${team.pointsFor}</td>
                                <td>${team.pointsAgainst}</td>
                                <td>${team.pointDifference > 0
                                    ? "+" + team.pointDifference
                                    : team.pointDifference}</td>
                                <td><strong>${team.competitionPoints}</strong></td>
                            </tr>
                        `).join("")}
                    </tbody>
                </table>
            </div>
        </div>
    `).join("");
}

// Populate tournament dropdown for Match Results
function populateResultsTournamentSelect(tournaments) {
    const select = document.getElementById("resultsTournamentSelect");

    select.innerHTML = '<option value="">Select a tournament</option>';

    tournaments.forEach(tournament => {
        const option = document.createElement("option");

        option.value = tournament.id;
        option.textContent =
            `${tournament.tournamentName} (${tournament.maxTeams} teams)`;

        select.appendChild(option);
    });

    if (tournaments.length === 0) {
        select.innerHTML =
            '<option value="">No tournaments available</option>';
    }
}

// Load match results for the selected tournament
async function loadResults() {
    const tournamentId =
        document.getElementById("resultsTournamentSelect").value;

    const resultsTableBody =
        document.getElementById("resultsTableBody");

    const message =
        document.getElementById("resultsMessage");

    message.textContent = "";

    if (!tournamentId) {
        resultsTableBody.innerHTML = `
            <tr>
                <td colspan="7">
                    Select a tournament to view match results.
                </td>
            </tr>
        `;
        return;
    }

    resultsTableBody.innerHTML = `
        <tr>
            <td colspan="7">Loading match results...</td>
        </tr>
    `;

    try {
        const response = await fetch(
            `${TOURNAMENT_API_URL}/${tournamentId}/results`
        );

        if (!response.ok) {
            throw new Error("Failed to retrieve match results.");
        }

        const results = await response.json();

        if (results.length === 0) {
            resultsTableBody.innerHTML = `
                <tr>
                    <td colspan="7">
                        No match results available for this tournament.
                    </td>
                </tr>
            `;
            return;
        }

        displayResults(results);

        message.textContent =
            `Loaded ${results.length} match results.`;

        message.style.color = "green";

    } catch (error) {
        console.error("Error loading match results:", error);

        resultsTableBody.innerHTML = `
            <tr>
                <td colspan="7">
                    Unable to load match results.
                </td>
            </tr>
        `;

        message.textContent = error.message;
        message.style.color = "red";
    }
}

// Display tournament match results

function displayResults(results) {
    const resultsTableBody =
        document.getElementById("resultsTableBody");

    resultsTableBody.innerHTML = results.map(result => {

        const status = result.resultStatus;

        let scoreHTML = "";
        let actionHTML = "";
        let statusClass = "";
        let statusLabel = "";

        if (status === "NOT_SUBMITTED") {

            statusClass = "result-status-upcoming";
            statusLabel = "NOT SUBMITTED";

            scoreHTML = `
                <div class="result-form">

                    <select
                        class="result-type-select"
                        id="resultType-${result.fixtureId}"
                    >
                        <option value="NORMAL">Normal</option>
                        <option value="FORFEIT">Forfeit</option>
                        <option value="NO_SHOW">No-show</option>
                        <option value="DEFAULT">Default</option>
                    </select>

                    <div
                        class="result-score-container"
                        id="scoreContainer-${result.fixtureId}"
                    >
                        <input
                            type="number"
                            min="0"
                            class="result-score-input"
                            id="teamAScore-${result.fixtureId}"
                            placeholder="A"
                        >

                        <span>-</span>

                        <input
                            type="number"
                            min="0"
                            class="result-score-input"
                            id="teamBScore-${result.fixtureId}"
                            placeholder="B"
                        >
                    </div>

                    <div
                        class="result-extra-fields"
                        id="extraFields-${result.fixtureId}"
                        hidden
                    >
                        <select
                            id="forfeitingTeam-${result.fixtureId}"
                        >
                            <option value="">Select responsible team</option>

                            <option value="${result.teamAId}">
                                ${escapeHTML(result.teamAName)}
                            </option>

                            <option value="${result.teamBId}">
                                ${escapeHTML(result.teamBName)}
                            </option>
                        </select>

                        <select
                            id="winnerKeepsScore-${result.fixtureId}"
                            hidden
                        >
                            <option value="">Winner keeps score?</option>
                            <option value="true">Yes</option>
                            <option value="false">No</option>
                        </select>
                    </div>
                </div>
            `;

            actionHTML = `
                <button
                    type="button"
                    class="result-action-button result-submit-button"
                    data-action="submit"
                    data-fixture-id="${result.fixtureId}"
                >
                    Submit Result
                </button>
            `;

        } else if (status === "PENDING_VERIFICATION") {

            statusClass = "result-status-pending";
            statusLabel = "PENDING VERIFICATION";

            scoreHTML = `
                <strong>
                    ${result.teamAScore} - ${result.teamBScore}
                </strong>
                <br>
                <small>${escapeHTML(result.resultType || "NORMAL")}</small>
            `;

            actionHTML = `
                <button
                    type="button"
                    class="result-action-button result-verify-button"
                    data-action="verify"
                    data-fixture-id="${result.fixtureId}"
                >
                    Verify Result
                </button>
            `;

        } else if (status === "COMPLETED") {

            statusClass = "result-status-completed";
            statusLabel = "COMPLETED";

            if (
                result.resultType === "FORFEIT" ||
                result.resultType === "NO_SHOW"
            ) {
                scoreHTML = `
                    <strong>
                        ${escapeHTML(result.winnerName || "Winner")}
                        (W–0)
                    </strong>
                    <br>
                    <small>${escapeHTML(result.resultType)}</small>
                `;
            } else if (result.resultType === "DEFAULT") {
                scoreHTML = `
                    <strong>
                        ${escapeHTML(result.winnerName || "Winner")}
                    </strong>
                    <br>
                    <small>DEFAULT</small>
                `;
            } else {
                scoreHTML = `
                    <strong>
                        ${result.teamAScore} - ${result.teamBScore}
                    </strong>
                `;
            }

            actionHTML = `<span>Verified</span>`;

        } else {

            statusClass = "result-status-upcoming";
            statusLabel = escapeHTML(status);
            scoreHTML = "-";
            actionHTML = "-";
        }

        return `
            <tr>
                <td>${result.fixtureId}</td>
                <td>${escapeHTML(result.groupName)}</td>
                <td>${escapeHTML(result.teamAName)}</td>
                <td>${scoreHTML}</td>
                <td>${escapeHTML(result.teamBName)}</td>

                <td>
                    <span class="result-status ${statusClass}">
                        ${statusLabel}
                    </span>
                </td>

                <td>${actionHTML}</td>
            </tr>
        `;

    }).join("");
}


function toggleResultFields(select) {

    const fixtureId = select.id.split("-")[1];

    const resultType = select.value;

    const scoreContainer =
        document.getElementById(`scoreContainer-${fixtureId}`);

    const extraFields =
        document.getElementById(`extraFields-${fixtureId}`);

    const forfeitingTeam =
        document.getElementById(`forfeitingTeam-${fixtureId}`);

    const winnerKeepsScore =
        document.getElementById(`winnerKeepsScore-${fixtureId}`);

    const requiresResponsibleTeam =
        ["FORFEIT", "NO_SHOW", "DEFAULT"].includes(resultType);

    extraFields.hidden = !requiresResponsibleTeam;

    scoreContainer.hidden =
        resultType === "FORFEIT" ||
        resultType === "NO_SHOW";

    forfeitingTeam.required = requiresResponsibleTeam;

    winnerKeepsScore.hidden = resultType !== "DEFAULT";
    winnerKeepsScore.required = resultType === "DEFAULT";

    if (resultType !== "DEFAULT") {
        winnerKeepsScore.value = "";
    }
}


// Submit a match result

async function submitMatchResult(fixtureId) {

    const message =
        document.getElementById("resultsMessage");

    const resultType =
        document.getElementById(`resultType-${fixtureId}`).value;

    const teamAScoreInput =
        document.getElementById(`teamAScore-${fixtureId}`);

    const teamBScoreInput =
        document.getElementById(`teamBScore-${fixtureId}`);

    const forfeitingTeamInput =
        document.getElementById(`forfeitingTeam-${fixtureId}`);

    const winnerKeepsScoreInput =
        document.getElementById(`winnerKeepsScore-${fixtureId}`);

    let teamAScore = 0;
    let teamBScore = 0;

    if (["NORMAL", "DEFAULT"].includes(resultType)) {

        if (
            teamAScoreInput.value.trim() === "" ||
            teamBScoreInput.value.trim() === ""
        ) {
            message.textContent = "Please enter both team scores.";
            message.style.color = "red";
            return;
        }

        teamAScore = Number(teamAScoreInput.value);
        teamBScore = Number(teamBScoreInput.value);

        if (
            !Number.isSafeInteger(teamAScore) ||
            !Number.isSafeInteger(teamBScore) ||
            teamAScore < 0 ||
            teamBScore < 0
        ) {
            message.textContent =
                "Scores must be non-negative whole numbers.";
            message.style.color = "red";
            return;
        }
    }

    if (
        resultType === "NORMAL" &&
        teamAScore === teamBScore
    ) {
        message.textContent =
            "Match scores cannot be tied.";
        message.style.color = "red";
        return;
    }

    let forfeitingTeamId = null;
    let winnerKeepsScore = null;

    if (resultType !== "NORMAL") {

        if (!forfeitingTeamInput.value) {
            message.textContent =
                "Please select the responsible team.";
            message.style.color = "red";
            return;
        }

        forfeitingTeamId =
            Number(forfeitingTeamInput.value);
    }

    if (resultType === "DEFAULT") {

        if (winnerKeepsScoreInput.value === "") {
            message.textContent =
                "Please specify whether the winner keeps the score.";
            message.style.color = "red";
            return;
        }

        winnerKeepsScore =
            winnerKeepsScoreInput.value === "true";
    }

    const button = document.querySelector(
        `button[data-action="submit"][data-fixture-id="${fixtureId}"]`
    );

    if (button) {
        button.disabled = true;
        button.textContent = "Submitting...";
    }

    try {

        const response = await fetch(
            `http://localhost:8080/api/fixtures/${fixtureId}/result`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    teamAScore,
                    teamBScore,
                    resultType,
                    forfeitingTeamId,
                    winnerKeepsScore
                })
            }
        );

        if (!response.ok) {

            const errorData =
                await response.json().catch(() => ({}));

            throw new Error(
                errorData.error ||
                "Failed to submit match result."
            );
        }

        await loadResults();

        message.textContent =
            `Fixture #${fixtureId} submitted successfully. Awaiting verification.`;

        message.style.color = "green";

    } catch (error) {

        console.error("Error submitting match result:", error);

        message.textContent = error.message;
        message.style.color = "red";

        if (button) {
            button.disabled = false;
            button.textContent = "Submit Result";
        }
    }
}


// Verify a submitted match result
async function verifyMatchResult(fixtureId) {
    const message =
        document.getElementById("resultsMessage");

    const button = document.querySelector(
        `button[data-action="verify"][data-fixture-id="${fixtureId}"]`
    );

    if (button) {
        button.disabled = true;
        button.textContent = "Verifying...";
    }

    try {
        const response = await fetch(
            `http://localhost:8080/api/fixtures/${fixtureId}/result/verify`,
            {
                method: "PATCH"
            }
        );

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));

            throw new Error(
                errorData.error || "Failed to verify match result."
            );
        }

        // Refresh results and standings after verification
        await loadResults();

        const resultsTournamentId =
            document.getElementById("resultsTournamentSelect").value;

        const standingsTournamentId =
            document.getElementById("standingsTournamentSelect").value;

        if (
            resultsTournamentId &&
            resultsTournamentId === standingsTournamentId
        ) {
            await loadStandings();
        }

        message.textContent =
            `Fixture #${fixtureId} result verified successfully.`;

        message.style.color = "green";

    } catch (error) {
        console.error("Error verifying match result:", error);

        message.textContent = error.message;
        message.style.color = "red";

        if (button) {
            button.disabled = false;
            button.textContent = "Verify Result";
        }
    }
}

// ========================================
// Prototype 0.8 - Sidebar Navigation
// ========================================

function initializeNavigation() {
    const navigationLinks = document.querySelectorAll(
        "#sidebarNavigation a[data-page]"
    );

    const pageSections = document.querySelectorAll(
        "[data-page-section]"
    );

    const pageTitle = document.querySelector(".topbar h1");
    const pageDescription = document.querySelector(".topbar p");

    const pageInformation = {
        dashboard: {
            title: "Dashboard",
            description: "Overview of your 3x3 basketball tournament."
        },
        tournaments: {
            title: "Tournaments",
            description: "Create and manage your tournaments."
        },
        teams: {
            title: "Teams",
            description: "Review and manage team registrations."
        },
        groups: {
            title: "Groups",
            description: "Generate and manage tournament groups."
        },
        fixtures: {
            title: "Fixtures",
            description: "Manage tournament match schedules."
        },
        results: {
            title: "Results",
            description: "Submit and verify match results."
        },
        standings: {
            title: "Standings",
            description: "View tournament group rankings."
        }
    };

    function showPage(pageName) {
        if (!pageInformation[pageName]) {
            pageName = "dashboard";
        }

        // Update sidebar active state
        navigationLinks.forEach(link => {
            const isActive = link.dataset.page === pageName;

            link.classList.toggle("active", isActive);

            if (isActive) {
                link.setAttribute("aria-current", "page");
            } else {
                link.removeAttribute("aria-current");
            }
        });

        // Show only the selected section
        pageSections.forEach(section => {
            const isActive =
                section.dataset.pageSection === pageName;

            section.classList.toggle("page-active", isActive);

            // Control visibility directly
            section.style.display = isActive ? "block" : "none";
        });

        // Update page heading
        pageTitle.textContent = pageInformation[pageName].title;

        pageDescription.textContent =
            pageInformation[pageName].description;

        // Return to the top when changing pages
        window.scrollTo(0, 0);
    }

    // Handle navigation clicks
    navigationLinks.forEach(link => {
        link.addEventListener("click", () => {
            showPage(link.dataset.page);
        });
    });

    // Support browser back/forward navigation
    window.addEventListener("hashchange", () => {
        const pageName = window.location.hash.substring(1);
        showPage(pageName);
    });

    // Display the correct page on initial load
    const initialPage = window.location.hash.substring(1);
    showPage(initialPage || "dashboard");
}