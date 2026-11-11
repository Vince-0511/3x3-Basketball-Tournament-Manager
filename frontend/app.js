
const API_URL = "http://localhost:8080/api/teams";

// Load teams when the page opens
document.addEventListener("DOMContentLoaded", () => {
    loadTeams();
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
