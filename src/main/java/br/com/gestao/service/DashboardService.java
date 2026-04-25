package br.com.gestao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestao.dto.dashboard.DashboardDTO;
import br.com.gestao.dto.dashboard.DashboardDTO.Activity;
import br.com.gestao.dto.dashboard.DashboardDTO.AdminSection;
import br.com.gestao.dto.dashboard.DashboardDTO.Alert;
import br.com.gestao.dto.dashboard.DashboardDTO.ChartPoint;
import br.com.gestao.dto.dashboard.DashboardDTO.DashboardPayload;
import br.com.gestao.dto.dashboard.DashboardDTO.Kpi;
import br.com.gestao.dto.dashboard.DashboardDTO.Legend;
import br.com.gestao.dto.dashboard.DashboardDTO.Metric;
import br.com.gestao.dto.dashboard.DashboardDTO.RankedItem;
import br.com.gestao.dto.dashboard.DashboardDTO.Shortcut;
import br.com.gestao.dto.dashboard.DashboardDTO.StatusCell;
import br.com.gestao.dto.dashboard.DashboardDTO.TableData;
import br.com.gestao.entity.Empresa;
import br.com.gestao.entity.Estoque;
import br.com.gestao.entity.Venda;
import br.com.gestao.entity.enums.StatusVenda;
import br.com.gestao.repository.EmpresaRepository;
import br.com.gestao.repository.EstoqueRepository;
import br.com.gestao.repository.LogErroAplicacaoRepository;
import br.com.gestao.repository.ParcelaRepository;
import br.com.gestao.repository.TituloAPagarRepository;
import br.com.gestao.repository.UsuarioRepository;
import br.com.gestao.repository.VendaItemRepository;
import br.com.gestao.repository.VendaRepository;
import br.com.gestao.repository.VersaoSistemaRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

	private final VendaRepository vendaRepository;
	private final VendaItemRepository vendaItemRepository;
	private final EstoqueRepository estoqueRepository;
	private final ParcelaRepository parcelaRepository;
	private final TituloAPagarRepository tituloAPagarRepository;
	private final UsuarioRepository usuarioRepository;
	private final EmpresaRepository empresaRepository;
	private final LogErroAplicacaoRepository logErroRepository;
	private final VersaoSistemaRepository versaoSistemaRepository;

	public DashboardService(
			VendaRepository vendaRepository,
			VendaItemRepository vendaItemRepository,
			EstoqueRepository estoqueRepository,
			ParcelaRepository parcelaRepository,
			TituloAPagarRepository tituloAPagarRepository,
			UsuarioRepository usuarioRepository,
			EmpresaRepository empresaRepository,
			LogErroAplicacaoRepository logErroRepository,
			VersaoSistemaRepository versaoSistemaRepository) {
		this.vendaRepository = vendaRepository;
		this.vendaItemRepository = vendaItemRepository;
		this.estoqueRepository = estoqueRepository;
		this.parcelaRepository = parcelaRepository;
		this.tituloAPagarRepository = tituloAPagarRepository;
		this.usuarioRepository = usuarioRepository;
		this.empresaRepository = empresaRepository;
		this.logErroRepository = logErroRepository;
		this.versaoSistemaRepository = versaoSistemaRepository;
	}

	public DashboardPayload buildEmpresaDashboard(Long empresaId) {
		LocalDate hoje = LocalDate.now();
		LocalDateTime hojeInicio = hoje.atStartOfDay();
		LocalDateTime hojeFim = hoje.atTime(LocalTime.MAX);
		LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();
		LocalDate seteAtras = hoje.minusDays(6);
		LocalDateTime seteAtrasDT = seteAtras.atStartOfDay();

		BigDecimal faturamentoHoje = vendaRepository.somarFaturamentoPeriodo(empresaId, hojeInicio, hojeFim);
		BigDecimal faturamentoMes = vendaRepository.somarFaturamentoPeriodo(empresaId, inicioMes, hojeFim);
		Long vendasHoje = vendaRepository.contarVendasPeriodo(empresaId, hojeInicio, hojeFim);
		BigDecimal ticketMedio = vendaRepository.calcularTicketMedioPeriodo(empresaId, inicioMes, hojeFim);
		Long estoqueCritico = estoqueRepository.contarAbaixoDoMinimo(empresaId);
		Long semEstoque = estoqueRepository.contarSemEstoque(empresaId);
		BigDecimal titulosVencidos = parcelaRepository.somarTotalVencido(empresaId);
		Long clientesInadimplentes = parcelaRepository.contarClientesInadimplentes(empresaId);
		BigDecimal aReceber = parcelaRepository.somarTotalEmAberto(empresaId);
		BigDecimal aPagar = tituloAPagarRepository.somarTotalEmAberto(empresaId);
		Long usuariosAtivos = usuarioRepository.contarAtivosPorEmpresa(empresaId);

		List<Kpi> overview = List.of(
				new Kpi("Faturamento Hoje", formatarMoeda(faturamentoHoje), "vendas finalizadas", "bi-currency-dollar", "var(--color-primary)"),
				new Kpi("Faturamento Mês", formatarMoeda(faturamentoMes), "acumulado do mês", "bi-graph-up-arrow", "var(--color-success)"),
				new Kpi("Vendas Hoje", String.valueOf(vendasHoje), "pedidos não cancelados", "bi-cart-check", "var(--color-accent)"),
				new Kpi("Ticket Médio", formatarMoeda(ticketMedio), "média do mês atual", "bi-receipt", "var(--color-primary)"),
				new Kpi("Estoque Crítico", estoqueCritico + " itens", "abaixo do mínimo", "bi-box-seam", "var(--color-danger)"),
				new Kpi("Títulos Vencidos", formatarMoeda(titulosVencidos), clientesInadimplentes + " clientes em atraso", "bi-exclamation-triangle", "var(--color-warning)")
		);

		List<Alert> alerts = buildEmpresaAlerts(estoqueCritico, titulosVencidos, clientesInadimplentes);

		List<Shortcut> shortcuts = List.of(
				new Shortcut("bi-bag-plus", "Nova venda", "Abrir PDV para venda rápida", "PDV", "/venda/pdv"),
				new Shortcut("bi-boxes", "Ajustar estoque", "Corrigir divergências e entradas", "Estoque", "/cadastro/estoque"),
				new Shortcut("bi-plus-square", "Cadastrar produto", "Adicionar SKU, grade e preço", "Cadastro", "/cadastro/produto"),
				new Shortcut("bi-person-plus", "Cadastrar cliente", "Criar cadastro para relacionamento", "CRM", "/cadastro/cliente")
		);

		List<Object[]> chartRows = vendaRepository.faturamentoPorDia(empresaId, seteAtrasDT, hojeFim);
		List<ChartPoint> salesChart = buildChartData(chartRows, seteAtras, hoje);

		double acumulado7d = salesChart.stream().mapToDouble(ChartPoint::value).sum();
		double mediaDiaria7d = acumulado7d / Math.max(salesChart.size(), 1);
		List<Legend> salesLegend = List.of(
				new Legend(formatarMilhares(acumulado7d), "Acumulado dos últimos 7 dias"),
				new Legend(formatarMilhares(mediaDiaria7d), "Média diária"),
				new Legend(formatarMoeda(faturamentoMes), "Faturamento do mês")
		);

		List<Object[]> topProdutosRows = vendaItemRepository.topProdutosPorQuantidade(empresaId, inicioMes, hojeFim, PageRequest.of(0, 4));
		List<RankedItem> topProducts = topProdutosRows.stream().map(row -> {
			String nome = (String) row[0];
			long qtd = ((Number) row[1]).longValue();
			BigDecimal receita = (BigDecimal) row[2];
			return new RankedItem(nome, qtd + " unidades", formatarMoeda(receita));
		}).collect(Collectors.toList());

		List<Object[]> topGruposRows = vendaItemRepository.topGruposPorReceita(empresaId, inicioMes, hojeFim, PageRequest.of(0, 4));
		BigDecimal totalReceita = topGruposRows.stream().map(r -> (BigDecimal) r[1]).reduce(BigDecimal.ZERO, BigDecimal::add);
		List<RankedItem> topCategories = topGruposRows.stream().map(row -> {
			String nome = (String) row[0];
			BigDecimal receita = (BigDecimal) row[1];
			String participacao = totalReceita.compareTo(BigDecimal.ZERO) > 0
					? String.format("%.0f%% da receita", receita.multiply(BigDecimal.valueOf(100)).divide(totalReceita, 0, RoundingMode.HALF_UP).doubleValue())
					: "0% da receita";
			return new RankedItem(nome != null ? nome : "Sem grupo", participacao, formatarMoeda(receita));
		}).collect(Collectors.toList());

		BigDecimal fluxo = aReceber.subtract(aPagar);
		List<Metric> financialKpis = List.of(
				new Metric(formatarMoeda(aReceber), "A receber (em aberto)"),
				new Metric(formatarMoeda(aPagar), "A pagar (em aberto)"),
				new Metric(formatarMoeda(fluxo), "Fluxo líquido")
		);

		TableData financialTable = new TableData(
				List.of("Indicador", "Valor", "Status"),
				List.of(
						row("A receber em aberto", formatarMoeda(aReceber), new StatusCell("Monitorado", "success")),
						row("A pagar em aberto", formatarMoeda(aPagar), new StatusCell("Monitorado", "success")),
						row("Inadimplência", formatarMoeda(titulosVencidos), titulosVencidos.compareTo(BigDecimal.valueOf(5000)) > 0 ? new StatusCell("Acompanhar", "warning") : new StatusCell("Controlado", "success")),
						row("Fluxo líquido", formatarMoeda(fluxo), fluxo.compareTo(BigDecimal.ZERO) >= 0 ? new StatusCell("Positivo", "success") : new StatusCell("Atenção", "danger"))
				)
		);

		List<Metric> stockSummary = List.of(
				new Metric(String.valueOf(estoqueCritico), "Produtos abaixo do mínimo"),
				new Metric(String.valueOf(semEstoque), "Sem estoque disponível"),
				new Metric("—", "Ajustes manuais hoje")
		);

		List<Estoque> criticos = estoqueRepository.listarCriticos(empresaId, PageRequest.of(0, 4));
		TableData stockTable = new TableData(
				List.of("Produto", "Estoque", "Mínimo", "Situação"),
				criticos.stream().map(e -> {
					String situacao;
					String tone;
					if (e.getQuantidade() == 0) {
						situacao = "Sem saldo";
						tone = "danger";
					} else if (e.getEstoqueMinimo() > 0 && e.getQuantidade() < e.getEstoqueMinimo() / 2) {
						situacao = "Reposição urgente";
						tone = "danger";
					} else {
						situacao = "Abaixo do mínimo";
						tone = "warning";
					}
					return row(e.getProduto().getDescricao(), String.valueOf(e.getQuantidade()), String.valueOf(e.getEstoqueMinimo()), new StatusCell(situacao, tone));
				}).collect(Collectors.toList())
		);

		List<Venda> ultimasVendas = vendaRepository.findUltimasVendasComCliente(empresaId, StatusVenda.CANCELADA, PageRequest.of(0, 5));
		List<Activity> activity = ultimasVendas.stream().limit(4).map(v -> {
			String cliente = v.getCliente() != null ? v.getCliente().getNome() : "Consumidor final";
			return new Activity("bi-cash-stack", "Venda #" + v.getId() + " — " + v.getStatus().getDescricao(), formatarMoeda(v.getValorTotal()) + " · " + cliente);
		}).collect(Collectors.toList());

		TableData salesTable = new TableData(
				List.of("Pedido", "Cliente", "Valor", "Status"),
				ultimasVendas.stream().map(v -> {
					String cliente = v.getCliente() != null ? v.getCliente().getNome() : "Consumidor final";
					String tone = v.getStatus() == StatusVenda.FINALIZADA ? "success" : "warning";
					return row("#" + v.getId(), cliente, formatarMoeda(v.getValorTotal()), new StatusCell(v.getStatus().getDescricao(), tone));
				}).collect(Collectors.toList())
		);

		AdminSection admin = new AdminSection(
				"Administração da Loja",
				"Equipe, operação e configurações internas.",
				"home-admin-card--empresa",
				List.of(
						new Metric(String.valueOf(usuariosAtivos), "Usuários ativos"),
						new Metric(String.valueOf(estoqueCritico), "Itens com estoque crítico"),
						new Metric(String.valueOf(semEstoque), "Produtos sem saldo")
				),
				new TableData(
						List.of("Frente", "Indicador", "Leitura"),
						List.of(
								row("Usuários", "Total ativos", String.valueOf(usuariosAtivos)),
								row("Estoque", "Abaixo do mínimo", String.valueOf(estoqueCritico)),
								row("Estoque", "Sem saldo", String.valueOf(semEstoque)),
								row("Financeiro", "Inadimplência total", formatarMoeda(titulosVencidos))
						)
				)
		);

		return new DashboardPayload(overview, alerts, shortcuts, salesChart, salesLegend, topProducts, topCategories, financialKpis, financialTable, stockSummary, stockTable, activity, salesTable, admin);
	}

	public DashboardPayload buildSaasDashboard() {
		LocalDate hoje = LocalDate.now();
		LocalDateTime hojeFim = hoje.atTime(LocalTime.MAX);
		LocalDate seteAtras = hoje.minusDays(6);
		LocalDateTime seteAtrasDT = seteAtras.atStartOfDay();

		Long empresasAtivas = empresaRepository.contarAtivas();
		Long usuariosAtivos = usuarioRepository.contarAtivosNaoSaas();
		Long emRisco = empresaRepository.contarEmRisco();
		Long erros24h = logErroRepository.contarApartirDe(LocalDateTime.now().minusHours(24));
		long totalVersoes = versaoSistemaRepository.count();

		List<Object[]> planoCount = empresaRepository.contagemPorPlano();
		BigDecimal mrrEstimado = calcularMrr(planoCount);

		List<Kpi> overview = List.of(
				new Kpi("Empresas Ativas", String.valueOf(empresasAtivas), "no ambiente", "bi-buildings", "var(--color-primary)"),
				new Kpi("MRR Estimado", formatarMoeda(mrrEstimado), "baseado nos planos ativos", "bi-cash-coin", "var(--color-success)"),
				new Kpi("Usuários Ativos", String.valueOf(usuariosAtivos), "vinculados a empresas", "bi-people", "var(--color-accent)"),
				new Kpi("Versões Publicadas", String.valueOf(totalVersoes), "versões do sistema", "bi-cloud-arrow-up", "var(--color-warning)"),
				new Kpi("Empresas em Risco", String.valueOf(emRisco), "vencidas ou suspensas", "bi-exclamation-triangle", "var(--color-danger)"),
				new Kpi("Erros 24h", String.valueOf(erros24h), "no log de aplicação", erros24h > 10 ? "bi-bug" : "bi-bug", "var(--color-danger)")
		);

		List<Alert> alerts = buildSaasAlerts(emRisco, erros24h);

		List<Shortcut> shortcuts = List.of(
				new Shortcut("bi-building-add", "Cadastrar empresa", "Nova conta e plano inicial", "SaaS", "/admin/empresas"),
				new Shortcut("bi-code-square", "Publicar versão", "Registrar release e changelog", "Deploy", "/admin/versoes"),
				new Shortcut("bi-journal-medical", "Revisar logs", "Auditar falhas recentes", "Suporte", "/admin/logs-erros"),
				new Shortcut("bi-cloud-arrow-up", "Ver versões", "Gerenciar versões do sistema", "Sistema", "/admin/versoes")
		);

		List<Object[]> chartRows = vendaRepository.faturamentoPorDiaPlataforma(seteAtrasDT, hojeFim);
		List<ChartPoint> salesChart = buildChartData(chartRows, seteAtras, hoje);

		double totalPlataforma = salesChart.stream().mapToDouble(ChartPoint::value).sum();
		List<Legend> salesLegend = List.of(
				new Legend(String.valueOf(empresasAtivas) + " contas", "Empresas ativas no ambiente"),
				new Legend(String.valueOf(usuariosAtivos), "Usuários com empresa vinculada"),
				new Legend(formatarMoeda(mrrEstimado), "MRR estimado")
		);

		List<RankedItem> topProducts = planoCount.stream().limit(4).map(row -> {
			String plano = (String) row[0];
			long count = ((Number) row[1]).longValue();
			BigDecimal mrr = planoMrr(plano).multiply(BigDecimal.valueOf(count));
			return new RankedItem("Plano " + capitalize(plano), count + " contas", formatarMoeda(mrr));
		}).collect(Collectors.toList());

		List<RankedItem> topCategories = List.of(
				new RankedItem("Ativas", empresasAtivas + " empresas", empresasAtivas > 0 ? "100%" : "0%"),
				new RankedItem("Em Risco", emRisco + " empresas", formatarPorcentagem(emRisco, empresasAtivas)),
				new RankedItem("Versões", totalVersoes + " publicadas", "sistema"),
				new RankedItem("Erros 24h", erros24h + " registros", erros24h > 10 ? "Atenção" : "Normal")
		);

		List<Metric> financialKpis = List.of(
				new Metric(formatarMoeda(mrrEstimado), "MRR estimado"),
				new Metric(String.valueOf(empresasAtivas), "Empresas ativas"),
				new Metric(String.valueOf(emRisco), "Empresas em risco")
		);

		TableData financialTable = new TableData(
				List.of("Indicador", "Valor", "Status"),
				List.of(
						row("MRR estimado", formatarMoeda(mrrEstimado), new StatusCell("Saudável", "success")),
						row("Empresas em risco", String.valueOf(emRisco), emRisco > 5 ? new StatusCell("Monitorar", "warning") : new StatusCell("Controlado", "success")),
						row("Erros (24h)", String.valueOf(erros24h), erros24h > 10 ? new StatusCell("Crítico", "danger") : new StatusCell("Normal", "success")),
						row("Versões publicadas", String.valueOf(totalVersoes), new StatusCell("Ativo", "success"))
				)
		);

		List<Metric> stockSummary = List.of(
				new Metric(String.valueOf(totalVersoes), "Versões publicadas"),
				new Metric(String.valueOf(erros24h), "Erros técnicos (24h)"),
				new Metric(String.valueOf(emRisco), "Empresas em risco")
		);

		List<Empresa> ultimasEmpresas = empresaRepository.findTop5ByOrderByIdDesc();
		TableData stockTable = new TableData(
				List.of("Empresa", "Plano", "Status", "Ativo"),
				ultimasEmpresas.stream().map(e -> {
					String tone = "ATIVA".equals(e.getStatus()) ? "success" : "warning";
					return row(e.getNomeFantasia(), capitalize(e.getPlano()), new StatusCell(e.getStatus(), tone), e.isAtivo() ? "Sim" : "Não");
				}).collect(Collectors.toList())
		);

		List<Activity> activity = new ArrayList<>();
		ultimasEmpresas.stream().limit(3).forEach(e -> activity.add(
				new Activity("bi-building-check", "Empresa: " + e.getNomeFantasia(), "Plano " + capitalize(e.getPlano()) + " · Status: " + e.getStatus())
		));
		if (erros24h > 0) {
			activity.add(new Activity("bi-bug", erros24h + " erros nas últimas 24h", "Verificar log de aplicação para detalhes"));
		}

		TableData salesTable = new TableData(
				List.of("Empresa", "Plano", "MRR", "Status"),
				ultimasEmpresas.stream().map(e -> {
					BigDecimal mrr = planoMrr(e.getPlano());
					String tone = "ATIVA".equals(e.getStatus()) ? "success" : "warning";
					return row(e.getNomeFantasia(), capitalize(e.getPlano()), formatarMoeda(mrr), new StatusCell(e.getStatus(), tone));
				}).collect(Collectors.toList())
		);

		AdminSection admin = new AdminSection(
				"Administração SaaS",
				"Saúde da base, crescimento e governança da plataforma.",
				"home-admin-card--saas",
				List.of(
						new Metric(String.valueOf(empresasAtivas), "Empresas ativas"),
						new Metric(String.valueOf(usuariosAtivos), "Usuários ativos"),
						new Metric(String.valueOf(erros24h), "Logs relevantes em 24h")
				),
				new TableData(
						List.of("Área", "Indicador", "Leitura"),
						List.of(
								row("Base", "Empresas ativas", String.valueOf(empresasAtivas)),
								row("Produto", "Versões publicadas", String.valueOf(totalVersoes)),
								row("Suporte", "Erros nas últimas 24h", String.valueOf(erros24h)),
								row("Risco", "Empresas com problemas", emRisco > 0 ? new StatusCell(emRisco + " empresas", "warning") : new StatusCell("Nenhuma", "success"))
						)
				)
		);

		return new DashboardPayload(overview, alerts, shortcuts, salesChart, salesLegend, topProducts, topCategories, financialKpis, financialTable, stockSummary, stockTable, activity, salesTable, admin);
	}

	private List<Alert> buildEmpresaAlerts(Long estoqueCritico, BigDecimal titulosVencidos, Long clientesInadimplentes) {
		List<Alert> alerts = new ArrayList<>();
		if (estoqueCritico > 0) {
			alerts.add(new Alert("bi-exclamation-diamond",
					"Estoque crítico em " + estoqueCritico + " SKU(s)",
					"Produtos abaixo do estoque mínimo — verifique e planeje reposição.",
					"Crítico"));
		}
		if (titulosVencidos.compareTo(BigDecimal.ZERO) > 0) {
			alerts.add(new Alert("bi-wallet2",
					"Recebíveis vencidos: " + formatarMoeda(titulosVencidos),
					clientesInadimplentes + " cliente(s) em atraso. Acione o setor financeiro.",
					"Financeiro"));
		}
		if (alerts.isEmpty()) {
			alerts.add(new Alert("bi-check-circle",
					"Nenhum alerta crítico no momento",
					"Todos os indicadores estão dentro do esperado.",
					"OK"));
		}
		return alerts;
	}

	private List<Alert> buildSaasAlerts(Long emRisco, Long erros24h) {
		List<Alert> alerts = new ArrayList<>();
		if (emRisco > 0) {
			alerts.add(new Alert("bi-cloud-slash",
					emRisco + " empresa(s) com risco operacional",
					"Com vencimento expirado ou status suspenso — revisar situação.",
					"Risco"));
		}
		if (erros24h > 10) {
			alerts.add(new Alert("bi-bug",
					erros24h + " erros registrados nas últimas 24h",
					"Verificar log de erros para identificar padrões recorrentes.",
					"Técnico"));
		}
		if (alerts.isEmpty()) {
			alerts.add(new Alert("bi-check-circle",
					"Plataforma operando normalmente",
					"Nenhum alerta crítico no momento.",
					"OK"));
		}
		return alerts;
	}

	private List<ChartPoint> buildChartData(List<Object[]> dbRows, LocalDate start, LocalDate end) {
		Map<LocalDate, BigDecimal> byDate = new LinkedHashMap<>();
		LocalDate current = start;
		while (!current.isAfter(end)) {
			byDate.put(current, BigDecimal.ZERO);
			current = current.plusDays(1);
		}
		for (Object[] row : dbRows) {
			if (row[0] == null) continue;
			LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
			BigDecimal total = row[1] instanceof BigDecimal bd ? bd : BigDecimal.valueOf(((Number) row[1]).doubleValue());
			byDate.put(date, total);
		}
		return byDate.entrySet().stream()
				.map(e -> new ChartPoint(
						getDayLabel(e.getKey().getDayOfWeek()),
						e.getValue().divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP).doubleValue()))
				.collect(Collectors.toList());
	}

	private String getDayLabel(DayOfWeek dow) {
		return switch (dow) {
			case MONDAY -> "Seg";
			case TUESDAY -> "Ter";
			case WEDNESDAY -> "Qua";
			case THURSDAY -> "Qui";
			case FRIDAY -> "Sex";
			case SATURDAY -> "Sab";
			case SUNDAY -> "Dom";
		};
	}

	private BigDecimal calcularMrr(List<Object[]> planoCount) {
		return planoCount.stream().map(row -> {
			String plano = (String) row[0];
			long count = ((Number) row[1]).longValue();
			return planoMrr(plano).multiply(BigDecimal.valueOf(count));
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal planoMrr(String plano) {
		if (plano == null) return BigDecimal.valueOf(150);
		return switch (plano.toUpperCase()) {
			case "PROFISSIONAL" -> BigDecimal.valueOf(350);
			case "ENTERPRISE" -> BigDecimal.valueOf(800);
			default -> BigDecimal.valueOf(150);
		};
	}

	private String formatarMoeda(BigDecimal value) {
		if (value == null) value = BigDecimal.ZERO;
		double v = value.doubleValue();
		if (Math.abs(v) >= 1_000_000) {
			return String.format("R$ %.1f mi", v / 1_000_000).replace('.', ',');
		} else if (Math.abs(v) >= 1_000) {
			return String.format("R$ %.1f mil", v / 1_000).replace('.', ',');
		} else {
			return String.format("R$ %.0f", v);
		}
	}

	private String formatarMilhares(double valueInThousands) {
		return String.format("R$ %.1f mil", valueInThousands).replace('.', ',');
	}

	private String formatarPorcentagem(Long parte, Long total) {
		if (total == null || total == 0) return "0%";
		return String.format("%.0f%%", (double) parte / total * 100);
	}

	private String capitalize(String s) {
		if (s == null || s.isBlank()) return s;
		String lower = s.toLowerCase();
		return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
	}

	private List<Object> row(Object... cells) {
		List<Object> list = new ArrayList<>();
		for (Object cell : cells) list.add(cell);
		return list;
	}
}
