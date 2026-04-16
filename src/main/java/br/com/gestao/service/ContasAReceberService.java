package br.com.gestao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestao.entity.ConfiguracaoFinanceira;
import br.com.gestao.entity.Parcela;
import br.com.gestao.entity.enums.FormaPagamento;
import br.com.gestao.entity.enums.StatusParcela;
import br.com.gestao.repository.ParcelaRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ContasAReceberService {

	private final ParcelaRepository parcelaRepository;
	private final ContextoUsuarioService contextoUsuarioService;
	private final ConfiguracaoFinanceiraService configuracaoFinanceiraService;
	private final CalculoFinanceiroService calculoFinanceiroService;
	private final LogFinanceiroService logFinanceiroService;

	public ContasAReceberService(ParcelaRepository parcelaRepository, ContextoUsuarioService contextoUsuarioService,
			ConfiguracaoFinanceiraService configuracaoFinanceiraService, CalculoFinanceiroService calculoFinanceiroService,
			LogFinanceiroService logFinanceiroService) {
		this.parcelaRepository = parcelaRepository;
		this.contextoUsuarioService = contextoUsuarioService;
		this.configuracaoFinanceiraService = configuracaoFinanceiraService;
		this.calculoFinanceiroService = calculoFinanceiroService;
		this.logFinanceiroService = logFinanceiroService;
	}

	public List<ResumoClienteDevedor> consultarResumoDevedores() {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		List<Object[]> rows = parcelaRepository.consultarResumoClientesDevedores(empresaId);
		List<ResumoClienteDevedor> resultado = new ArrayList<>(rows.size());
		for (Object[] r : rows) {
			ResumoClienteDevedor item = new ResumoClienteDevedor();
			item.setClienteId(((Number) r[0]).longValue());
			item.setNomeCliente((String) r[1]);
			item.setTotalAberto(r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO);
			item.setQtdVencidas(r[3] != null ? ((Number) r[3]).intValue() : 0);
			item.setProximoVencimento(r[4] != null ? toLocalDate(r[4]) : null);
			resultado.add(item);
		}
		return resultado;
	}

	public IndicadoresFinanceiros calcularIndicadores() {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		IndicadoresFinanceiros ind = new IndicadoresFinanceiros();
		ind.setTotalAReceber(nvl(parcelaRepository.somarTotalEmAberto(empresaId)));
		ind.setTotalVencido(nvl(parcelaRepository.somarTotalVencido(empresaId)));
		YearMonth mesAtual = YearMonth.now();
		LocalDate inicio = mesAtual.atDay(1);
		LocalDate fim = mesAtual.atEndOfMonth();
		ind.setRecebimentosMes(nvl(parcelaRepository.somarRecebimentosPeriodo(empresaId, inicio, fim)));
		Long inadimp = parcelaRepository.contarClientesInadimplentes(empresaId);
		ind.setClientesInadimplentes(inadimp != null ? inadimp : 0L);
		ind.setUltimaAtualizacaoJuros(parcelaRepository.ultimaAtualizacaoJuros(empresaId));
		return ind;
	}

	public List<Parcela> listarParcelasCliente(Long clienteId) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		return parcelaRepository.findAllByEmpresaIdAndClienteIdOrderByDataVencimentoAsc(empresaId, clienteId);
	}

	public List<Parcela> filtrar(StatusParcela status, LocalDate inicio, LocalDate fim, Long clienteId) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		return parcelaRepository.filtrar(empresaId, status, inicio, fim, clienteId);
	}

	public Parcela consultarPorId(Long id) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		return parcelaRepository.findByIdAndEmpresaId(id, empresaId)
				.orElseThrow(() -> new EntityNotFoundException("Parcela nao encontrada!"));
	}

	/**
	 * Recalcula juros/multa de uma parcela usando configuracao atual.
	 * Nao persiste se a parcela ja estiver paga.
	 */
	@Transactional
	public Parcela recalcularJurosParcela(Long parcelaId) {
		Parcela parcela = consultarPorId(parcelaId);
		if (parcela.getStatus() == StatusParcela.PAGO) {
			return parcela;
		}
		ConfiguracaoFinanceira config = configuracaoFinanceiraService.obterOuCriarPadrao();
		BigDecimal antes = parcela.getValorTotalAtualizado();
		calculoFinanceiroService.calcularEncargos(parcela, config, LocalDate.now());
		parcela.setJurosAtualizadosEm(LocalDateTime.now());
		if (parcela.getDataVencimento().isBefore(LocalDate.now()) && parcela.getStatus() == StatusParcela.PENDENTE) {
			parcela.setStatus(StatusParcela.VENCIDO);
		}
		Parcela salva = parcelaRepository.save(parcela);
		logFinanceiroService.registrar("RECALCULO_JUROS", salva, antes, salva.getValorTotalAtualizado(),
				"Recalculo manual de encargos");
		return salva;
	}

	@Transactional
	public int recalcularJurosCliente(Long clienteId) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		List<Parcela> parcelas = parcelaRepository.findAllByEmpresaIdAndClienteIdAndStatusIn(empresaId, clienteId,
				List.of(StatusParcela.PENDENTE, StatusParcela.VENCIDO));
		ConfiguracaoFinanceira config = configuracaoFinanceiraService.obterOuCriarPadrao();
		int atualizadas = 0;
		for (Parcela parcela : parcelas) {
			BigDecimal antes = parcela.getValorTotalAtualizado();
			calculoFinanceiroService.calcularEncargos(parcela, config, LocalDate.now());
			parcela.setJurosAtualizadosEm(LocalDateTime.now());
			if (parcela.getDataVencimento().isBefore(LocalDate.now()) && parcela.getStatus() == StatusParcela.PENDENTE) {
				parcela.setStatus(StatusParcela.VENCIDO);
			}
			parcelaRepository.save(parcela);
			logFinanceiroService.registrar("RECALCULO_JUROS", parcela, antes, parcela.getValorTotalAtualizado(),
					"Recalculo em lote para cliente");
			atualizadas++;
		}
		return atualizadas;
	}

	@Transactional
	public Parcela quitar(QuitacaoForm form) throws Exception {
		if (form == null || form.getParcelaId() == null) {
			throw new Exception("Parcela invalida!");
		}
		if (form.getValorRecebido() == null || form.getValorRecebido().signum() <= 0) {
			throw new Exception("Valor recebido invalido!");
		}
		if (form.getFormaPagamento() == null) {
			throw new Exception("Informe a forma de pagamento!");
		}
		Parcela parcela = consultarPorId(form.getParcelaId());
		if (parcela.getStatus() == StatusParcela.PAGO) {
			throw new Exception("Parcela ja quitada!");
		}

		// Atualiza juros/multa ate a data do pagamento antes de quitar
		LocalDate dataPagamento = form.getDataPagamento() != null ? form.getDataPagamento() : LocalDate.now();
		ConfiguracaoFinanceira config = configuracaoFinanceiraService.obterOuCriarPadrao();
		if (parcela.getDataVencimento().isBefore(dataPagamento)) {
			calculoFinanceiroService.calcularEncargos(parcela, config, dataPagamento);
		}

		BigDecimal valorAntes = parcela.getValorTotalAtualizado();
		parcela.setValorPago(form.getValorRecebido().setScale(2, RoundingMode.HALF_UP));
		parcela.setDataPagamento(dataPagamento);
		parcela.setFormaPagamentoQuitacao(form.getFormaPagamento());
		parcela.setStatus(StatusParcela.PAGO);
		if (form.getObservacao() != null && !form.getObservacao().isBlank()) {
			String atual = parcela.getObservacao() != null ? parcela.getObservacao() + " | " : "";
			parcela.setObservacao((atual + form.getObservacao()).trim());
		}

		Parcela salva = parcelaRepository.save(parcela);
		logFinanceiroService.registrar("QUITACAO", salva, valorAntes, salva.getValorPago(),
				"Parcela " + salva.getNumeroParcela() + "/" + salva.getTotalParcelas()
						+ " quitada em " + dataPagamento + " via " + form.getFormaPagamento());
		return salva;
	}

	/**
	 * Job: marca parcelas vencidas e atualiza juros/multa.
	 */
	@Transactional
	public int processarInadimplenciaGlobal() {
		LocalDate hoje = LocalDate.now();
		List<Parcela> parcelas = parcelaRepository.findGlobalByStatusInAndDataVencimentoLessThan(
				List.of(StatusParcela.PENDENTE, StatusParcela.VENCIDO), hoje);
		int atualizadas = 0;
		for (Parcela p : parcelas) {
			ConfiguracaoFinanceira config = configuracaoFinanceiraService.obterParaEmpresa(p.getEmpresa().getId(), p.getEmpresa());
			BigDecimal antes = p.getValorTotalAtualizado();
			calculoFinanceiroService.calcularEncargos(p, config, hoje);
			if (p.getStatus() == StatusParcela.PENDENTE) {
				p.setStatus(StatusParcela.VENCIDO);
			}
			p.setJurosAtualizadosEm(LocalDateTime.now());
			parcelaRepository.save(p);
			logFinanceiroService.registrar("JOB_INADIMPLENCIA", p, antes, p.getValorTotalAtualizado(),
					"Atualizacao automatica diaria");
			atualizadas++;
		}
		return atualizadas;
	}

	private BigDecimal nvl(BigDecimal v) {
		return v != null ? v : BigDecimal.ZERO;
	}

	private LocalDate toLocalDate(Object valor) {
		if (valor instanceof LocalDate ld) {
			return ld;
		}
		if (valor instanceof java.sql.Date sd) {
			return sd.toLocalDate();
		}
		if (valor instanceof java.util.Date d) {
			return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
		}
		return LocalDate.parse(valor.toString());
	}

	// ------ Form DTOs / View Models ------

	public static class QuitacaoForm {
		private Long parcelaId;
		private BigDecimal valorRecebido;
		private LocalDate dataPagamento;
		private FormaPagamento formaPagamento;
		private String observacao;

		public Long getParcelaId() { return parcelaId; }
		public void setParcelaId(Long parcelaId) { this.parcelaId = parcelaId; }
		public BigDecimal getValorRecebido() { return valorRecebido; }
		public void setValorRecebido(BigDecimal valorRecebido) { this.valorRecebido = valorRecebido; }
		public LocalDate getDataPagamento() { return dataPagamento; }
		public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
		public FormaPagamento getFormaPagamento() { return formaPagamento; }
		public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
		public String getObservacao() { return observacao; }
		public void setObservacao(String observacao) { this.observacao = observacao; }
	}

	public static class ResumoClienteDevedor {
		private Long clienteId;
		private String nomeCliente;
		private BigDecimal totalAberto;
		private Integer qtdVencidas;
		private LocalDate proximoVencimento;

		public Long getClienteId() { return clienteId; }
		public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
		public String getNomeCliente() { return nomeCliente; }
		public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
		public BigDecimal getTotalAberto() { return totalAberto; }
		public void setTotalAberto(BigDecimal totalAberto) { this.totalAberto = totalAberto; }
		public Integer getQtdVencidas() { return qtdVencidas; }
		public void setQtdVencidas(Integer qtdVencidas) { this.qtdVencidas = qtdVencidas; }
		public LocalDate getProximoVencimento() { return proximoVencimento; }
		public void setProximoVencimento(LocalDate proximoVencimento) { this.proximoVencimento = proximoVencimento; }
	}

	public static class IndicadoresFinanceiros {
		private BigDecimal totalAReceber = BigDecimal.ZERO;
		private BigDecimal totalVencido = BigDecimal.ZERO;
		private BigDecimal recebimentosMes = BigDecimal.ZERO;
		private Long clientesInadimplentes = 0L;
		private LocalDateTime ultimaAtualizacaoJuros;

		public BigDecimal getTotalAReceber() { return totalAReceber; }
		public void setTotalAReceber(BigDecimal totalAReceber) { this.totalAReceber = totalAReceber; }
		public BigDecimal getTotalVencido() { return totalVencido; }
		public void setTotalVencido(BigDecimal totalVencido) { this.totalVencido = totalVencido; }
		public BigDecimal getRecebimentosMes() { return recebimentosMes; }
		public void setRecebimentosMes(BigDecimal recebimentosMes) { this.recebimentosMes = recebimentosMes; }
		public Long getClientesInadimplentes() { return clientesInadimplentes; }
		public void setClientesInadimplentes(Long clientesInadimplentes) { this.clientesInadimplentes = clientesInadimplentes; }
		public LocalDateTime getUltimaAtualizacaoJuros() { return ultimaAtualizacaoJuros; }
		public void setUltimaAtualizacaoJuros(LocalDateTime ultimaAtualizacaoJuros) { this.ultimaAtualizacaoJuros = ultimaAtualizacaoJuros; }
	}

}
