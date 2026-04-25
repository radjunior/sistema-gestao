document.addEventListener("DOMContentLoaded", async function () {
	const root = document.getElementById("home-dashboard");
	if (!root) return;

	try {
		const res = await fetch("/api/dashboard", { headers: { "Accept": "application/json" } });
		if (!res.ok) throw new Error("HTTP " + res.status);
		const data = await res.json();
		renderDashboard(data);
	} catch (err) {
		console.error("Erro ao carregar dashboard:", err);
		root.insertAdjacentHTML("afterbegin",
			'<div class="alert alert-warning mx-3 mt-3">Não foi possível carregar os dados do dashboard. Tente recarregar a página.</div>'
		);
	}
});

function renderDashboard(data) {
	renderOverview(document.getElementById("home-overview"), data.overview);
	renderAlerts(document.getElementById("home-alert-list"), document.getElementById("home-alert-badge"), data.alerts);
	renderShortcuts(document.getElementById("home-shortcuts"), data.shortcuts);
	renderSalesChart(document.getElementById("home-sales-chart"), data.salesChart);
	renderLegend(document.getElementById("home-sales-legend"), data.salesLegend);
	renderRankedList(document.getElementById("home-top-products"), data.topProducts);
	renderRankedList(document.getElementById("home-top-categories"), data.topCategories);
	renderMetricCards(document.getElementById("home-financial-kpis"), data.financialKpis, "home-financial-kpis__item");
	renderTable(document.getElementById("home-financial-table"), data.financialTable);
	renderMetricCards(document.getElementById("home-stock-summary"), data.stockSummary, "home-stock-summary__item");
	renderTable(document.getElementById("home-stock-table"), data.stockTable);
	renderActivity(document.getElementById("home-activity-list"), data.activity);
	renderTable(document.getElementById("home-sales-table"), data.salesTable);
	renderAdmin(
		document.getElementById("home-admin-card"),
		document.getElementById("home-admin-title"),
		document.getElementById("home-admin-subtitle"),
		document.getElementById("home-admin-summary"),
		document.getElementById("home-admin-table"),
		data.admin
	);
}

function renderOverview(target, items) {
	if (!target || !items) return;
	target.innerHTML = items.map((item) => `
		<article class="home-kpi" style="--kpi-accent:${item.accent};">
			<div class="home-kpi__label">
				<span>${item.label}</span>
				<span class="home-kpi__icon"><i class="bi ${item.icon}"></i></span>
			</div>
			<div class="home-kpi__value">${item.value}</div>
			<div class="home-kpi__meta">${item.meta}</div>
		</article>
	`).join("");
}

function renderAlerts(target, badge, items) {
	if (!target || !items) return;
	if (badge) badge.textContent = items.length;
	target.innerHTML = items.map((item) => `
		<article class="home-alert-item">
			<div class="home-alert-item__icon"><i class="bi ${item.icon}"></i></div>
			<div class="home-alert-item__content">
				<h3 class="home-alert-item__title">${item.title}</h3>
				<p class="home-alert-item__text">${item.text}</p>
			</div>
			<span class="home-alert-item__pill">${item.pill}</span>
		</article>
	`).join("");
}

function renderShortcuts(target, items) {
	if (!target || !items) return;
	target.innerHTML = items.map((item) => `
		<a href="${item.href || "#"}" class="home-shortcut text-decoration-none">
			<div class="home-shortcut__icon"><i class="bi ${item.icon}"></i></div>
			<div class="home-shortcut__content">
				<h3 class="home-shortcut__title">${item.title}</h3>
				<p class="home-shortcut__meta">${item.meta}</p>
			</div>
			<span class="home-shortcut__pill">${item.pill}</span>
		</a>
	`).join("");
}

function renderSalesChart(target, items) {
	if (!target || !items || items.length === 0) return;
	const max = Math.max(...items.map((item) => item.value), 0.1);
	target.innerHTML = items.map((item) => {
		const height = Math.max(8, (item.value / max) * 100);
		return `
			<div class="home-chart__bar-wrap">
				<div class="home-chart__value">R$ ${item.value.toFixed(1)} mil</div>
				<div class="home-chart__bar">
					<div class="home-chart__bar-fill" style="height:${height}%"></div>
				</div>
				<div class="home-chart__label">${item.label}</div>
			</div>
		`;
	}).join("");
}

function renderLegend(target, items) {
	if (!target || !items) return;
	target.innerHTML = items.map((item) => `
		<div class="home-chart-legend__item">
			<strong>${item.value}</strong>
			<span>${item.label}</span>
		</div>
	`).join("");
}

function renderRankedList(target, items) {
	if (!target || !items) return;
	if (items.length === 0) {
		target.innerHTML = '<p class="text-muted small mt-2">Nenhum dado disponível no período.</p>';
		return;
	}
	target.innerHTML = `
		<div class="home-ranked-list">
			${items.map((item, index) => `
				<div class="home-ranked-list__item">
					<div class="home-ranked-list__index">${index + 1}</div>
					<div>
						<div class="home-ranked-list__name">${item.name}</div>
						<div class="home-ranked-list__meta">${item.meta}</div>
					</div>
					<div class="home-ranked-list__value">${item.value}</div>
				</div>
			`).join("")}
		</div>
	`;
}

function renderMetricCards(target, items, className) {
	if (!target || !items) return;
	target.innerHTML = items.map((item) => `
		<div class="${className}">
			<strong>${item.value}</strong>
			<span>${item.label}</span>
		</div>
	`).join("");
}

function renderActivity(target, items) {
	if (!target || !items) return;
	if (items.length === 0) {
		target.innerHTML = '<p class="text-muted small mt-2">Nenhuma atividade recente.</p>';
		return;
	}
	target.innerHTML = items.map((item) => `
		<article class="home-activity-item">
			<div class="home-activity-item__icon"><i class="bi ${item.icon}"></i></div>
			<div class="home-activity-item__content">
				<h3 class="home-activity-item__title">${item.title}</h3>
				<p class="home-activity-item__meta">${item.meta}</p>
			</div>
		</article>
	`).join("");
}

function renderAdmin(card, title, subtitle, summary, table, admin) {
	if (!card || !admin) return;
	card.classList.add(admin.cardClass);
	if (title) title.textContent = admin.title;
	if (subtitle) subtitle.textContent = admin.subtitle;
	renderMetricCards(summary, admin.summary, "home-admin-summary__item");
	renderTable(table, admin.table);
}

function renderTable(target, table) {
	if (!target || !table) return;
	if (!table.rows || table.rows.length === 0) {
		target.innerHTML = '<p class="text-muted small mt-2">Nenhum dado disponível.</p>';
		return;
	}
	target.innerHTML = `
		<table class="home-mini-table__table">
			<thead>
				<tr>
					${table.columns.map((column) => `<th>${column}</th>`).join("")}
				</tr>
			</thead>
			<tbody>
				${table.rows.map((row) => `
					<tr>
						${row.map((cell) => `<td>${renderCell(cell)}</td>`).join("")}
					</tr>
				`).join("")}
			</tbody>
		</table>
	`;
}

function renderCell(cell) {
	if (typeof cell === "object" && cell !== null && "status" in cell) {
		return `<span class="home-status home-status--${cell.tone || "success"}">${cell.status}</span>`;
	}
	return cell ?? "—";
}
