const API_BASE = "http://localhost:8080/api";

function getAuthHeader() {
    const token = localStorage.getItem("token");
    return {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
    };
}

async function login() {
    const u = document.getElementById("login-username").value;
    const p = document.getElementById("login-password").value;
    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: u, password: p })
        });
        const data = await res.json();
        if (res.ok) {
            localStorage.setItem("token", data.token);
            localStorage.setItem("username", data.username);
            window.location.href = "dashboard.html";
        } else {
            showAuthError("Invalid username or password");
        }
    } catch (e) {
        showAuthError("Server connection failed");
    }
}

async function register() {
    const u = document.getElementById("reg-username").value;
    const e = document.getElementById("reg-email").value;
    const p = document.getElementById("reg-password").value;
    try {
        const res = await fetch(`${API_BASE}/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: u, email: e, password: p })
        });
        const data = await res.json();
        if (res.ok) {
            toggleAuth(true); // switch to login
            alert("Registration successful! Please sign in.");
        } else {
            showAuthError(data.message || "Registration failed");
        }
    } catch (err) {
        showAuthError("Server connection failed");
    }
}

function showAuthError(msg) {
    const el = document.getElementById("auth-error");
    el.innerText = msg;
    el.classList.remove("hidden");
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    window.location.href = "index.html";
}

// ---- Dashboard Logic ----
async function loadDashboard() {
    await loadHabits();
    await loadGamificationStats();
    renderHeatmapMockup();
}

async function loadHabits() {
    try {
        const res = await fetch(`${API_BASE}/habits`, { headers: getAuthHeader() });
        if (res.status === 401) return logout();
        const habits = await res.json();
        const listEl = document.getElementById("habits-list");
        listEl.innerHTML = "";

        if (habits.length === 0) {
            listEl.innerHTML = "<p style='color: #cbd5e1'>No habits yet. Let's start building one!</p>";
            return;
        }

        habits.forEach(h => {
            listEl.innerHTML += `
                <div class="habit-item">
                    <div class="habit-info">
                        <h3>${h.title}</h3>
                        <p>${h.frequencyType} ${h.description ? '- ' + h.description : ''}</p>
                    </div>
                    <div class="habit-meta">
                        <span class="streak-badge">🔥 ${h.currentStreak}</span>
                        <button class="btn-icon" onclick="openEditModal(${h.id}, '${h.title.replace(/'/g, "\\'")}', '${(h.description || '').replace(/'/g, "\\'")}', '${h.frequencyType}')" title="Edit">✏️</button>
                        <button class="btn-icon" style="color: #ef4444" onclick="deleteHabit(${h.id})" title="Delete">🗑️</button>
                        <button class="btn-complete" onclick="logHabit(${h.id})">Complete</button>
                    </div>
                </div>
            `;
        });

        // Render real analytics after habits are loaded
        renderAnalyticsChart(habits);
    } catch (e) { console.error("Error loading habits", e); }
}

async function logHabit(id) {
    try {
        const res = await fetch(`${API_BASE}/habits/${id}/log`, {
            method: "POST",
            headers: getAuthHeader(),
            body: JSON.stringify({ notes: "Completed via web" })
        });
        if (res.ok) {
            loadDashboard(); // reload to update streaks and XP
            alert("Great job!");
        } else {
            const err = await res.json();
            alert(err.message || "Could not log habit");
        }
    } catch (e) { console.error(e); }
}

async function createHabit() {
    const title = document.getElementById("habit-title").value;
    const desc = document.getElementById("habit-desc").value;
    const freq = document.getElementById("habit-freq").value;

    if (!title) return alert("Title required");

    try {
        const res = await fetch(`${API_BASE}/habits`, {
            method: "POST",
            headers: getAuthHeader(),
            body: JSON.stringify({ title, description: desc, frequencyType: freq, targetDays: "" })
        });
        if (res.ok) {
            closeModal("habit-modal");
            document.getElementById("habit-title").value = "";
            document.getElementById("habit-desc").value = "";
            loadDashboard();
        }
    } catch (e) { console.error(e); }
}

function openEditModal(id, title, desc, freq) {
    document.getElementById("edit-habit-id").value = id;
    document.getElementById("edit-habit-title").value = title;
    document.getElementById("edit-habit-desc").value = desc;
    document.getElementById("edit-habit-freq").value = freq;
    openModal('edit-habit-modal');
}

async function updateHabit() {
    const id = document.getElementById("edit-habit-id").value;
    const title = document.getElementById("edit-habit-title").value;
    const desc = document.getElementById("edit-habit-desc").value;
    const freq = document.getElementById("edit-habit-freq").value;

    if (!title) return alert("Title required");

    try {
        const res = await fetch(`${API_BASE}/habits/${id}`, {
            method: "PUT",
            headers: getAuthHeader(),
            body: JSON.stringify({ title, description: desc, frequencyType: freq, targetDays: "" })
        });
        if (res.ok) {
            closeModal("edit-habit-modal");
            loadDashboard();
        } else {
            alert("Failed to update habit.");
        }
    } catch (e) { console.error(e); }
}

async function deleteHabit(id) {
    if (!confirm("Are you sure you want to delete this habit and all its history?")) return;
    try {
        const res = await fetch(`${API_BASE}/habits/${id}`, {
            method: "DELETE",
            headers: getAuthHeader()
        });
        if (res.ok) {
            loadDashboard();
        } else {
            alert("Failed to delete habit.");
        }
    } catch (e) { console.error(e); }
}

async function loadGamificationStats() {
    try {
        const res = await fetch(`${API_BASE}/habits/analytics/gamification`, { headers: getAuthHeader() });
        if (res.ok) {
            const stats = await res.json();
            document.getElementById("nav-level").innerText = `Level ${stats.level}`;
            document.getElementById("nav-xp").innerText = `${stats.xp} XP`;
        }
    } catch (e) { console.error(e); }
}

let habitChartInstance = null;
async function renderAnalyticsChart(habits) {
    const ctx = document.getElementById('habitChart');
    if (!ctx) return;

    // Destroy existing chart to prevent overlap
    if (habitChartInstance) {
        habitChartInstance.destroy();
    }

    const labels = habits.map(h => h.title);
    const data = habits.map(h => h.currentStreak);

    habitChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Current Streak 🔥',
                data: data,
                backgroundColor: 'rgba(99, 102, 241, 0.6)',
                borderColor: 'rgba(99, 102, 241, 1)',
                borderWidth: 1,
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1,
                        color: '#94a3b8'
                    },
                    grid: { color: 'rgba(255,255,255,0.05)' }
                },
                x: {
                    ticks: { color: '#94a3b8' },
                    grid: { display: false }
                }
            },
            plugins: {
                legend: { labels: { color: '#e2e8f0' } }
            }
        }
    });
}

function openModal(id) { document.getElementById(id).classList.remove("hidden"); }
function closeModal(id) { document.getElementById(id).classList.add("hidden"); }

// ---- AI CHAT ----
function appendChat(msg, isUser) {
    const chatWin = document.getElementById("chat-window");
    const div = document.createElement("div");
    div.className = `chat-bubble ${isUser ? 'user-bubble' : 'ai-bubble'}`;
    div.innerText = msg;
    chatWin.appendChild(div);
    chatWin.scrollTop = chatWin.scrollHeight;
}

function handleChatEnter(e) {
    if (e.key === "Enter") sendChatMessage();
}

async function sendChatMessage() {
    const input = document.getElementById("chat-input");
    const msg = input.value.trim();
    if (!msg) return;

    appendChat(msg, true);
    input.value = "";

    try {
        const response = await puter.ai.chat(msg);
        appendChat(response.message.content || response.toString(), false);
    } catch (e) {
        console.error(e);
        appendChat("Oops, I couldn't connect to my AI core.", false);
    }
}

async function getSmartInsights() {
    try {
        // In a real scenario, you'd fetch the user's habit data here first and pass it to Puter.
        // For now, we'll ask Puter to give a general insightful productivity tip.
        const response = await puter.ai.chat("Give me a short, 2-sentence productivity insight or motivational tip for a habit tracking user.");
        appendChat("💡 " + (response.message.content || response.toString()), false);
    } catch (e) {
        console.error(e);
        appendChat("Failed to fetch insights.", false);
    }
}
