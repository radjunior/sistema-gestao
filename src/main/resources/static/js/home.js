const dashboardData = {
	empresa: {
		overview: [
			{ label: "Faturamento Hoje", value: "R$ 12.480", meta: "+8,4% vs ontem", icon: "bi-currency-dollar", accent: "var(--color-primary)" },
			{ label: "Faturamento Mes", value: "R$ 286.900", meta: "Meta: R$ 320 mil", icon: "bi-graph-up-arrow", accent: "var(--color-success)" },
			{ label: "Vendas Hoje", value: "47", meta: "6 aguardando pagamento", icon: "bi-cart-check", accent: "var(--color-accent)" },
			{ label: "Ticket Medio", value: "R$ 265", meta: "Estavel nos ultimos 7 dias", icon: "bi-receipt", accent: "var(--color-primary)" },
			{ label: "Estoque Critico", value: "18 itens", meta: "5 sem reposicao prevista", icon: "bi-box-seam", accent: "var(--color-danger)" },
			{ label: "Titulos Vencidos", value: "R$ 8.230", meta: "12 clientes em atraso", icon: "bi-exclamation-triangle", accent: "var(--color-warning)" }
		],
		alerts: [
			{ icon: "bi-exclamation-diamond", title: "Estoque abaixo do minimo em 18 SKUs", text: "Linha infantil e jeans concentraram 62% das rupturas previstas para esta semana.", pill: "Critico" },
			{ icon: "bi-wallet2", title: "Recebiveis vencidos acima do limite", text: "Clientes com atraso acima de 15 dias somam R$ 8.230 e ja exigem contato do financeiro.", pill: "Financeiro" },
			{ icon: "bi-arrow-left-right", title: "Ajustes manuais de estoque acima da media", text: "Foram 23 ajustes nas ultimas 24h; vale revisar recebimento e conferencia.", pill: "Operacao" }
		],
		shortcuts: [
			{ icon: "bi-bag-plus", title: "Nova venda", meta: "Abrir PDV para venda rapida", pill: "PDV" },
			{ icon: "bi-boxes", title: "Ajustar estoque", meta: "Corrigir divergencias e entradas", pill: "Estoque" },
			{ icon: "bi-plus-square", title: "Cadastrar produto", meta: "Adicionar SKU, grade e preco", pill: "Cadastro" },
			{ icon: "bi-person-plus", title: "Cadastrar cliente", meta: "Criar cadastro para relacionamento", pill: "CRM" }
		],
		salesChart: [
			{ label: "Seg", value: 18.2 },
			{ label: "Ter", value: 21.4 },
			{ label: "Qua", value: 19.7 },
			{ label: "Qui", value: 24.9 },
			{ label: "Sex", value: 28.1 },
			{ label: "Sab", value: 35.6 },
			{ label: "Dom", value: 14.3 }
		],
		salesLegend: [
			{ value: "R$ 162 mil", label: "Acumulado da semana" },
			{ value: "R$ 23,1 mil", label: "Media diaria" },
			{ value: "32,4%", label: "Participacao do sabado" }
		],
		topProducts: [
			{ name: "Calca Jeans Reta 219", meta: "126 unidades", value: "R$ 18.940" },
			{ name: "Blusa Ribana Basic", meta: "118 unidades", value: "R$ 12.180" },
			{ name: "Vestido Midi Flora", meta: "87 unidades", value: "R$ 16.250" },
			{ name: "Jaqueta Sarja Urban", meta: "64 unidades", value: "R$ 14.720" }
		],
		topCategories: [
			{ name: "Jeans", meta: "31% da receita", value: "R$ 88.900" },
			{ name: "Blusas", meta: "22% da receita", value: "R$ 63.140" },
			{ name: "Vestidos", meta: "19% da receita", value: "R$ 54.960" },
			{ name: "Acessorios", meta: "11% da receita", value: "R$ 31.400" }
		],
		financialKpis: [
			{ value: "R$ 42.800", label: "A receber nesta semana" },
			{ value: "R$ 18.600", label: "A pagar nesta semana" },
			{ value: "R$ 24.200", label: "Fluxo liquido previsto" }
		],
		financialTable: {
			columns: ["Indicador", "Valor", "Status"],
			rows: [
				["Contas a receber hoje", "R$ 12.480", { status: "Dentro do esperado", tone: "success" }],
				["Contas a pagar hoje", "R$ 4.320", { status: "Baixo risco", tone: "success" }],
				["Inadimplencia > 15 dias", "R$ 8.230", { status: "Acompanhar", tone: "warning" }],
				["Caixa projetado D+7", "R$ 61.900", { status: "Saudavel", tone: "success" }]
			]
		},
		stockSummary: [
			{ value: "18", label: "Produtos abaixo do minimo" },
			{ value: "5", label: "Sem estoque disponivel" },
			{ value: "23", label: "Ajustes manuais hoje" }
		],
		stockTable: {
			columns: ["Produto", "Estoque", "Minimo", "Situacao"],
			rows: [
				["Calca Jeans Reta 219 / 38", "2", "8", { status: "Reposicao urgente", tone: "danger" }],
				["Blusa Ribana Basic / M", "4", "10", { status: "Abaixo do minimo", tone: "warning" }],
				["Vestido Midi Flora / G", "0", "4", { status: "Sem saldo", tone: "danger" }],
				["Jaqueta Sarja Urban / P", "6", "6", { status: "No limite", tone: "warning" }]
			]
		},
		activity: [
			{ icon: "bi-cash-stack", title: "Venda #8491 finalizada", meta: "Ha 9 minutos por Ana Paula no PDV central." },
			{ icon: "bi-box-arrow-in-down", title: "Entrada de estoque registrada", meta: "46 unidades da linha jeans adicionadas ao deposito principal." },
			{ icon: "bi-person-check", title: "Cliente fidelidade atualizado", meta: "Cadastro de Marcela Torres recebeu novo limite de desconto." },
			{ icon: "bi-journal-check", title: "Conta a pagar conciliada", meta: "Fornecedor Urban Basic baixado pelo financeiro." }
		],
		salesTable: {
			columns: ["Pedido", "Cliente", "Valor", "Status"],
			rows: [
				["#8491", "Marcela Torres", "R$ 328,00", { status: "Pago", tone: "success" }],
				["#8490", "Consumidor final", "R$ 189,90", { status: "Pago", tone: "success" }],
				["#8489", "Juliana Alves", "R$ 512,40", { status: "Separacao", tone: "warning" }],
				["#8488", "Renata Prado", "R$ 279,00", { status: "Entrega", tone: "success" }],
				["#8487", "Patricia Gomes", "R$ 145,00", { status: "Pendente", tone: "warning" }]
			]
		},
		admin: {
			title: "Administracao da Loja",
			subtitle: "Equipe, operacao e configuracoes internas.",
			cardClass: "home-admin-card--empresa",
			summary: [
				{ value: "12", label: "Usuarios ativos" },
				{ value: "3", label: "Permissoes pendentes de revisao" },
				{ value: "97%", label: "Conformidade cadastral" }
			],
			table: {
				columns: ["Frente", "Indicador", "Leitura"],
				rows: [
					["Usuarios", "Ultimo acesso gerencial", "Hoje, 08:12"],
					["Configuracao", "Financeiro parametrizado", { status: "Concluido", tone: "success" }],
					["Cadastro", "Produtos sem foto", "14 itens"],
					["Operacao", "Pedidos aguardando separacao", "6 pedidos"]
				]
			}
		}
	},
	saas: {
		overview: [
			{ label: "Empresas Ativas", value: "64", meta: "3 novas nesta semana", icon: "bi-buildings", accent: "var(--color-primary)" },
			{ label: "MRR Estimado", value: "R$ 128.400", meta: "+4,1% no mes", icon: "bi-cash-coin", accent: "var(--color-success)" },
			{ label: "Usuarios Ativos", value: "418", meta: "82% da base acessou em 7 dias", icon: "bi-people", accent: "var(--color-accent)" },
			{ label: "Versoes Pendentes", value: "9", meta: "2 exigem comunicacao", icon: "bi-cloud-arrow-up", accent: "var(--color-warning)" },
			{ label: "Empresas em Risco", value: "7", meta: "Baixo uso ou inadimplencia", icon: "bi-exclamation-triangle", accent: "var(--color-danger)" },
			{ label: "Erros 24h", value: "12", meta: "3 concentrados em importacao", icon: "bi-bug", accent: "var(--color-danger)" }
		],
		alerts: [
			{ icon: "bi-cloud-slash", title: "7 empresas com risco operacional", text: "Baixa frequencia de acesso e atraso financeiro acima do esperado no ciclo atual.", pill: "Risco" },
			{ icon: "bi-bug", title: "Erros recorrentes na rotina de importacao", text: "Tres clientes tiveram falhas repetidas ao importar catalogo em lote nas ultimas 24h.", pill: "Tecnico" },
			{ icon: "bi-megaphone", title: "Versao 2.8.0 ainda nao visualizada", text: "Nove contas ainda nao confirmaram leitura das alteracoes liberadas hoje.", pill: "Comunicacao" }
		],
		shortcuts: [
			{ icon: "bi-building-add", title: "Cadastrar empresa", meta: "Nova conta e plano inicial", pill: "SaaS" },
			{ icon: "bi-person-badge", title: "Criar admin inicial", meta: "Liberar acesso a nova loja", pill: "Acesso" },
			{ icon: "bi-code-square", title: "Publicar versao", meta: "Registrar release e changelog", pill: "Deploy" },
			{ icon: "bi-journal-medical", title: "Revisar logs", meta: "Auditar falhas recentes", pill: "Suporte" }
		],
		salesChart: [
			{ label: "Seg", value: 12 },
			{ label: "Ter", value: 18 },
			{ label: "Qua", value: 14 },
			{ label: "Qui", value: 21 },
			{ label: "Sex", value: 25 },
			{ label: "Sab", value: 19 },
			{ label: "Dom", value: 10 }
		],
		salesLegend: [
			{ value: "64 contas", label: "Empresas ativas no ambiente" },
			{ value: "5,8 dias", label: "Tempo medio para ativacao" },
			{ value: "91%", label: "Retencao de 90 dias" }
		],
		topProducts: [
			{ name: "Plano Profissional", meta: "28 contas", value: "R$ 67.200" },
			{ name: "Plano Essencial", meta: "24 contas", value: "R$ 33.600" },
			{ name: "Modulo PDV", meta: "17 ativacoes", value: "R$ 10.540" },
			{ name: "Modulo Financeiro", meta: "12 ativacoes", value: "R$ 8.220" }
		],
		topCategories: [
			{ name: "Moda", meta: "22 empresas", value: "34%" },
			{ name: "Calcados", meta: "14 empresas", value: "21%" },
			{ name: "Acessorios", meta: "11 empresas", value: "17%" },
			{ name: "Multi-loja", meta: "7 empresas", value: "11%" }
		],
		financialKpis: [
			{ value: "R$ 18.900", label: "Receita prevista nesta semana" },
			{ value: "R$ 4.200", label: "Recebiveis em atraso" },
			{ value: "R$ 124.200", label: "MRR projetado pos-renovacoes" }
		],
		financialTable: {
			columns: ["Indicador", "Valor", "Status"],
			rows: [
				["MRR atual", "R$ 128.400", { status: "Saudavel", tone: "success" }],
				["Churn previsto", "2,4%", { status: "Controlado", tone: "success" }],
				["Inadimplencia", "R$ 4.200", { status: "Monitorar", tone: "warning" }],
				["Upsell pipeline", "R$ 11.800", { status: "Oportunidade", tone: "success" }]
			]
		},
		stockSummary: [
			{ value: "9", label: "Versoes aguardando leitura" },
			{ value: "12", label: "Chamados tecnicos nas ultimas 24h" },
			{ value: "4", label: "Implantacoes em andamento" }
		],
		stockTable: {
			columns: ["Empresa", "Frente", "Situacao", "Leitura"],
			rows: [
				["Avenida Fashion", "Implantacao", { status: "Em andamento", tone: "warning" }, "Checklist 82%" ],
				["Loja Lume", "Suporte", { status: "Erro critico", tone: "danger" }, "Importacao de catalogo" ],
				["Veste Bem", "Comunicacao", { status: "Pendente", tone: "warning" }, "Versao 2.8.0" ],
				["Moda Base", "Financeiro", { status: "Atraso", tone: "danger" }, "12 dias" ]
			]
		},
		activity: [
			{ icon: "bi-building-check", title: "Empresa 'Nova Trama' ativada", meta: "Plano profissional liberado com 4 usuarios e PDV." },
			{ icon: "bi-cloud-upload", title: "Versao 2.8.0 publicada", meta: "Ajustes de dashboard e correcoes no fluxo de estoque." },
			{ icon: "bi-headset", title: "Chamado #302 resolvido", meta: "Erro de sincronizacao fiscal encerrado pelo suporte." },
			{ icon: "bi-person-gear", title: "Admin inicial criado", meta: "Conta da empresa Luar Kids liberada para onboarding." }
		],
		salesTable: {
			columns: ["Empresa", "Plano", "MRR", "Status"],
			rows: [
				["Avenida Fashion", "Profissional", "R$ 3.200", { status: "Ativa", tone: "success" }],
				["Loja Lume", "Essencial", "R$ 1.200", { status: "Atencao", tone: "warning" }],
				["Nova Trama", "Profissional", "R$ 2.800", { status: "Implantacao", tone: "warning" }],
				["Moda Base", "Enterprise", "R$ 5.900", { status: "Ativa", tone: "success" }],
				["Luar Kids", "Essencial", "R$ 1.100", { status: "Trial", tone: "success" }]
			]
		},
		admin: {
			title: "Administracao SaaS",
			subtitle: "Saude da base, crescimento e governanca da plataforma.",
			cardClass: "home-admin-card--saas",
			summary: [
				{ value: "64", label: "Empresas ativas" },
				{ value: "418", label: "Usuarios ativos" },
				{ value: "12", label: "Logs relevantes em 24h" }
			],
			table: {
				columns: ["Area", "Indicador", "Leitura"],
				rows: [
					["Base", "Empresas em onboarding", "4 contas"],
					["Produto", "Versoes pendentes", "9 contas"],
					["Suporte", "Chamados abertos", "12 tickets"],
					["Seguranca", "Politicas revisadas", { status: "Em dia", tone: "success" }]
				]
			}
		}
	}
};

document.addEventListener("DOMContentLoaded", function() {
	const root = document.getElementById("home-dashboard");
	if (!root) {
		return;
	}

	const role = root.dataset.dashboardRole === "saas" ? "saas" : "empresa";
	const data = dashboardData[role];

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
	renderAdmin(document.getElementById("home-admin-card"), document.getElementById("home-admin-title"), document.getElementById("home-admin-subtitle"), document.getElementById("home-admin-summary"), document.getElementById("home-admin-table"), data.admin);
});

function renderOverview(target, items) {
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
	badge.textContent = items.length;
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
	target.innerHTML = items.map((item) => `
		<article class="home-shortcut">
			<div class="home-shortcut__icon"><i class="bi ${item.icon}"></i></div>
			<div class="home-shortcut__content">
				<h3 class="home-shortcut__title">${item.title}</h3>
				<p class="home-shortcut__meta">${item.meta}</p>
			</div>
			<span class="home-shortcut__pill">${item.pill}</span>
		</article>
	`).join("");
}

function renderSalesChart(target, items) {
	const max = Math.max(...items.map((item) => item.value));
	target.innerHTML = items.map((item) => {
		const height = Math.max(10, (item.value / max) * 100);
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
	target.innerHTML = items.map((item) => `
		<div class="home-chart-legend__item">
			<strong>${item.value}</strong>
			<span>${item.label}</span>
		</div>
	`).join("");
}

function renderRankedList(target, items) {
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
	target.innerHTML = items.map((item) => `
		<div class="${className}">
			<strong>${item.value}</strong>
			<span>${item.label}</span>
		</div>
	`).join("");
}

function renderActivity(target, items) {
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
	card.classList.add(admin.cardClass);
	title.textContent = admin.title;
	subtitle.textContent = admin.subtitle;
	renderMetricCards(summary, admin.summary, "home-admin-summary__item");
	renderTable(table, admin.table);
}

function renderTable(target, table) {
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
	return cell;
}
