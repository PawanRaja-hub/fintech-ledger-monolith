const state = {
    token: localStorage.getItem("ledger.token") || "",
    email: localStorage.getItem("ledger.email") || "",
    role: localStorage.getItem("ledger.role") || "",
    wallet: null,
    wallets: []
};

const password = "Password@123";
const money = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" });

const $ = (selector) => document.querySelector(selector);

function toast(message) {
    const box = $("#toast");
    box.textContent = message;
    box.classList.add("show");
    clearTimeout(box.timer);
    box.timer = setTimeout(() => box.classList.remove("show"), 3600);
}

async function api(path, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };
    if (state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }
    const response = await fetch(path, { ...options, headers });
    const text = await response.text();
    const body = text ? JSON.parse(text) : {};
    if (!response.ok) {
        throw new Error(body.message || `Request failed with ${response.status}`);
    }
    return body;
}

function setSession(email, role, token) {
    state.email = email;
    state.role = role;
    state.token = token;
    localStorage.setItem("ledger.email", email);
    localStorage.setItem("ledger.role", role);
    localStorage.setItem("ledger.token", token);
    $("#session-label").textContent = `${email} signed in as ${role}`;
}

async function login(email) {
    const body = await api("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password })
    });
    setSession(email, body.data.role, body.data.token);
    await refreshAll();
    toast(`Signed in as ${email}`);
}

function renderMetrics() {
    $("#balance-value").textContent = state.wallet ? money.format(Number(state.wallet.availableBalance)) : "$0.00";
    $("#wallet-id").textContent = state.wallet?.walletId ?? "-";
}

async function loadMyWallet() {
    if (!state.token) return;
    try {
        const body = await api("/api/wallets/me");
        state.wallet = body.data;
    } catch (error) {
        state.wallet = null;
    }
    renderMetrics();
}

function renderWalletSelectors() {
    const options = state.wallets.map(wallet => {
        const label = `#${wallet.walletId} - ${wallet.ownerEmail} (${money.format(Number(wallet.availableBalance))})`;
        return `<option value="${wallet.walletId}">${label}</option>`;
    }).join("");
    $("#fund-wallet").innerHTML = options || `<option value="">No wallets visible</option>`;
    $("#to-wallet").innerHTML = state.wallets
        .filter(wallet => wallet.walletId !== state.wallet?.walletId)
        .map(wallet => `<option value="${wallet.walletId}">#${wallet.walletId} - ${wallet.ownerEmail}</option>`)
        .join("") || `<option value="">Sign in as admin to load recipients</option>`;
}

function renderWalletList() {
    $("#wallet-list").innerHTML = state.wallets.map(wallet => `
        <div class="row">
            <strong>${wallet.ownerEmail}</strong>
            <span>Wallet #${wallet.walletId}</span>
            <span>${money.format(Number(wallet.availableBalance))}</span>
            <span>Version ${wallet.version}</span>
            <span class="pill">Active</span>
        </div>
    `).join("");
}

async function loadWallets() {
    if (!state.token) return;
    try {
        const body = await api("/api/wallets/recipients");
        state.wallets = body.data;
    } catch (error) {
        state.wallets = [];
    }
    renderWalletSelectors();
    renderWalletList();
}

function statusClass(status, matched) {
    if (matched === false || status === "BLOCKED" || status === "REJECTED") return "danger";
    if (status === "PENDING_REVIEW") return "warn";
    return "";
}

async function loadReviews() {
    if (!state.token) return;
    try {
        const body = await api("/api/admin/reviews");
        $("#review-count").textContent = body.data.length;
        $("#review-list").innerHTML = body.data.map(tx => `
            <div class="row">
                <strong>${tx.reference}</strong>
                <span>${money.format(Number(tx.amount))}</span>
                <span>Risk ${tx.riskScore}</span>
                <span class="pill ${statusClass(tx.status)}">${tx.status}</span>
                <div class="actions">
                    <button type="button" data-review="approve" data-reference="${tx.reference}">Approve</button>
                    <button class="ghost" type="button" data-review="reject" data-reference="${tx.reference}">Reject</button>
                </div>
            </div>
        `).join("") || `<div class="row"><strong>No pending reviews</strong><span>Risk queue is clear</span></div>`;
    } catch (error) {
        $("#review-count").textContent = "-";
        $("#review-list").innerHTML = `<div class="row"><strong>Reviews unavailable</strong><span>${error.message}</span></div>`;
    }
}

async function loadReconciliation() {
    if (!state.token) return;
    try {
        const body = await api("/api/admin/reconciliation");
        const matched = body.data.filter(row => row.matched).length;
        $("#match-rate").textContent = `${matched}/${body.data.length}`;
        $("#reconcile-list").innerHTML = body.data.map(row => `
            <div class="row">
                <strong>${row.ownerEmail}</strong>
                <span>Wallet ${money.format(Number(row.walletBalance))}</span>
                <span>Ledger ${money.format(Number(row.ledgerBalance))}</span>
                <span>Wallet #${row.walletId}</span>
                <span class="pill ${statusClass(null, row.matched)}">${row.matched ? "MATCHED" : "DRIFT"}</span>
            </div>
        `).join("");
    } catch (error) {
        $("#match-rate").textContent = "-";
        $("#reconcile-list").innerHTML = `<div class="row"><strong>Reconciliation unavailable</strong><span>${error.message}</span></div>`;
    }
}

async function refreshAll() {
    await loadMyWallet();
    await loadWallets();
    await loadReviews();
    await loadReconciliation();
}

document.addEventListener("click", async (event) => {
    const tab = event.target.closest(".tab");
    if (tab) {
        document.querySelectorAll(".tab").forEach(item => item.classList.remove("is-active"));
        document.querySelectorAll(".panel").forEach(item => item.classList.remove("is-active"));
        tab.classList.add("is-active");
        $(`#${tab.dataset.panel}`).classList.add("is-active");
    }

    const action = event.target.dataset.review;
    if (action) {
        try {
            await api(`/api/admin/reviews/${event.target.dataset.reference}/${action}`, { method: "POST" });
            await refreshAll();
            toast(`Payment ${action}d`);
        } catch (error) {
            toast(error.message);
        }
    }
});

$("#login-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        await login($("#demo-user").value);
    } catch (error) {
        toast(error.message);
    }
});

$("#transfer-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        const result = await api("/api/payments/transfers", {
            method: "POST",
            headers: { "Idempotency-Key": `ui-${crypto.randomUUID()}` },
            body: JSON.stringify({
                toWalletId: Number($("#to-wallet").value),
                amount: $("#transfer-amount").value,
                memo: $("#transfer-memo").value
            })
        });
        $("#transfer-result").textContent = `${result.data.reference} created with ${result.data.status}`;
        await refreshAll();
    } catch (error) {
        $("#transfer-result").textContent = error.message;
        toast(error.message);
    }
});

$("#fund-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        await api("/api/admin/wallets/fund", {
            method: "POST",
            body: JSON.stringify({
                walletId: Number($("#fund-wallet").value),
                amount: $("#fund-amount").value
            })
        });
        $("#fund-result").textContent = "Wallet funded from system cash account";
        await refreshAll();
    } catch (error) {
        $("#fund-result").textContent = error.message;
        toast(error.message);
    }
});

$("#refresh-wallet").addEventListener("click", refreshAll);
$("#refresh-wallets").addEventListener("click", loadWallets);
$("#refresh-reviews").addEventListener("click", loadReviews);
$("#refresh-reconcile").addEventListener("click", loadReconciliation);

if (state.token) {
    $("#session-label").textContent = `${state.email} signed in as ${state.role}`;
    refreshAll();
}
