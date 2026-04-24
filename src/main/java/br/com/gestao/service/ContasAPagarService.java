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
import br.com.gestao.entity.Empresa;
import br.com.gestao.entity.Fornecedor;
import br.com.gestao.entity.TituloAPagar;
import br.com.gestao.entity.enums.CategoriaDespesa;
import br.com.gestao.entity.enums.FormaPagamento;
import br.com.gestao.entity.enums.StatusParcela;
import br.com.gestao.repository.FornecedorRepository;
import br.com.gestao.repository.TituloAPagarRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ContasAPagarService {

	private final TituloAPagarRepository tituloRepository;
	private final FornecedorRepository fornecedorRepository;
	private final ContextoUsuarioService contextoUsuarioService;
	private final ConfiguracaoFinanceiraService configuracaoFinanceiraService;
	private final CalculoFinanceiroService calculoFinanceiroService;
	private final LogFinanceiroService logFinanceiroService;

	public ContasAPagarService(TituloAPagarRepository tituloRepository,
			FornecedorRepository fornecedorRepository,
			ContextoUsuarioService contextoUsuarioService,
			ConfiguracaoFinanceiraService configuracaoFinanceiraService,
			CalculoFinanceiroService calculoFinanceiroService,
			LogFinanceiroService logFinanceiroService) {
		this.tituloRepository = tituloRepository;
		this.fornecedorRepository = fornecedorRepository;
		this.contextoUsuarioService = contextoUsuarioService;
		this.configuracaoFinanceiraService = configuracaoFinanceiraService;
		this.calculoFinanceiroService = calculoFinanceiroService;
		this.logFinanceiroService = logFinanceiroService;
	}

	public IndicadoresContasAPagar calcularIndicadores() {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		IndicadoresContasAPagar ind = new IndicadoresContasAPagar();
		ind.setTotalAPagar(nvl(tituloRepository.somarTotalEmAberto(empresaId)));
		ind.setTotalVencido(nvl(tituloRepository.somarTotalVencido(empresaId)));
		YearMonth mesAtual = YearMonth.now();
		ind.setPagamentosMes(nvl(tituloRepository.somarPagamentosPeriodo(empresaId,
				mesAtual.atDay(1), mesAtual.atEndOfMonth())));
		Long atrasados = tituloRepository.contarFornecedoresEmAtraso(empresaId);
		ind.setFornecedoresEmAtraso(atrasados != null ? atrasados : 0L);
		ind.setUltimaAtualizacaoJuros(tituloRepository.ultimaAtualizacaoJuros(empresaId));
		return ind;
	}

	public List<ResumoFornecedorCredor> consultarResumoCredores() {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		List<Object[]> rows = tituloRepository.consultarResumoFornecedoresCredores(empresaId);
		List<ResumoFornecedorCredor> resultado = new ArrayList<>(rows.size());
		for (Object[] r : rows) {
			ResumoFornecedorCredor item = new ResumoFornecedorCredor();
			item.setFornecedorId(((Number) r[0]).longValue());
			item.setNomeFornecedor((String) r[1]);
			item.setTotalAberto(r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO);
			item.setQtdVencidas(r[3] != null ? ((Number) r[3]).intValue() : 0);
			item.setProximoVencimento(r[4] != null ? toLocalDate(r[4]) : null);
			resultado.add(item);
		}
		return resultado;
	}

	public List<TituloAPagar> listarTitulosFornecedor(Long fornecedorId) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		return tituloRepository.findAllByEmpresaIdAndFornecedorIdOrderByDataVencimentoAsc(empresaId, fornecedorId);
	}

	public List<TituloAPagar> filtrar(StatusParcela status, LocalDate inicio, LocalDate fim, Long fornecedorId) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		return tituloRepository.filtrar(empresaId, status, inicio, fim, fornecedorId);
	}

	public TituloAPagar consultarPorId(Long id) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		return tituloRepository.findByIdAndEmpresaId(id, empresaId)
				.orElseThrow(() -> new EntityNotFoundException("Título não encontrado!"));
	}

	@Transactional
	public List<TituloAPagar> cadastrar(TituloForm form) throws Exception {
		validarForm(form);
		Empresa empresa = contextoUsuarioService.getEmpresaLogada();
		Fornecedor fornecedor = null;
		if (form.getFornecedorId() != null) {
			fornecedor = fornecedorRepository.findByIdAndEmpresaId(form.getFornecedorId(), empresa.getId())
					.orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado!"));
		}

		int qtd = form.getTotalParcelas() != null && form.getTotalParcelas() > 0 ? form.getTotalParcelas() : 1;
		List<BigDecimal> valores = calculoFinanceiroService.calcularParcelasSemJuros(form.getValorTotal(), qtd);
		LocalDate base = form.getDataPrimeiroVencimento();
		List<TituloAPagar> salvos = new ArrayList<>(qtd);
		for (int i = 0; i < qtd; i++) {
			TituloAPagar t = new TituloAPagar();
			t.setEmpresa(empresa);
			t.setFornecedor(fornecedor);
			t.setCategoriaDespesa(form.getCategoriaDespesa() != null ? form.getCategoriaDespesa() : CategoriaDespesa.OUTROS);
			t.setDescricao(form.getDescricao());
			t.setDocumento(form.getDocumento());
			t.setNumeroParcela(i + 1);
			t.setTotalParcelas(qtd);
			t.setValorNominal(valores.get(i));
			t.setDataEmissao(form.getDataEmissao());
			t.setDataVencimento(base.plusMonths(i));
			t.setStatus(StatusParcela.PENDENTE);
			t.setObservacao(form.getObservacao());
			TituloAPagar salvo = tituloRepository.save(t);
			logFinanceiroService.registrarTitulo("CADASTRO_TITULO_PAGAR", salvo, null, salvo.getValorNominal(),
					"Cadastro de título " + salvo.getNumeroParcela() + "/" + salvo.getTotalParcelas()
							+ " - " + salvo.getDescricao());
			salvos.add(salvo);
		}
		return salvos;
	}

	@Transactional
	public TituloAPagar atualizar(Long id, TituloForm form) throws Exception {
		validarForm(form);
		TituloAPagar titulo = consultarPorId(id);
		if (titulo.getStatus() == StatusParcela.PAGO) {
			throw new Exception("Título já pago não pode ser editado.");
		}
		Empresa empresa = titulo.getEmpresa();
		Fornecedor fornecedor = null;
		if (form.getFornecedorId() != null) {
			fornecedor = fornecedorRepository.findByIdAndEmpresaId(form.getFornecedorId(), empresa.getId())
					.orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado!"));
		}
		BigDecimal antes = titulo.getValorNominal();
		titulo.setFornecedor(fornecedor);
		titulo.setCategoriaDespesa(form.getCategoriaDespesa() != null ? form.getCategoriaDespesa() : CategoriaDespesa.OUTROS);
		titulo.setDescricao(form.getDescricao());
		titulo.setDocumento(form.getDocumento());
		titulo.setValorNominal(form.getValorTotal().setScale(2, RoundingMode.HALF_UP));
		titulo.setDataEmissao(form.getDataEmissao());
		titulo.setDataVencimento(form.getDataPrimeiroVencimento());
		titulo.setObservacao(form.getObservacao());
		TituloAPagar salvo = tituloRepository.save(titulo);
		logFinanceiroService.registrarTitulo("EDICAO_TITULO_PAGAR", salvo, antes, salvo.getValorNominal(),
				"Edição manual do título");
		return salvo;
	}

	@Transactional
	public void excluir(Long id) throws Exception {
		TituloAPagar titulo = consultarPorId(id);
		if (titulo.getStatus() == StatusParcela.PAGO) {
			throw new Exception("Título já pago não pode ser excluído.");
		}
		logFinanceiroService.registrarTitulo("EXCLUSAO_TITULO_PAGAR", titulo, titulo.getValorNominal(), null,
				"Exclusão de título " + titulo.getNumeroParcela() + "/" + titulo.getTotalParcelas());
		tituloRepository.delete(titulo);
	}

	@Transactional
	public TituloAPagar quitar(QuitacaoTituloForm form) throws Exception {
		if (form == null || form.getTituloId() == null) {
			throw new Exception("Título inválido!");
		}
		if (form.getValorPago() == null || form.getValorPago().signum() <= 0) {
			throw new Exception("Valor pago inválido!");
		}
		if (form.getFormaPagamento() == null) {
			throw new Exception("Informe a forma de pagamento!");
		}
		TituloAPagar titulo = consultarPorId(form.getTituloId());
		if (titulo.getStatus() == StatusParcela.PAGO) {
			throw new Exception("Título já quitado!");
		}

		LocalDate dataPagamento = form.getDataPagamento() != null ? form.getDataPagamento() : LocalDate.now();
		ConfiguracaoFinanceira config = configuracaoFinanceiraService.obterOuCriarPadrao();
		if (titulo.getDataVencimento().isBefore(dataPagamento)) {
			calculoFinanceiroService.calcularEncargos(titulo, config, dataPagamento);
		}

		BigDecimal valorAntes = titulo.getValorTotalAtualizado();
		titulo.setValorPago(form.getValorPago().setScale(2, RoundingMode.HALF_UP));
		titulo.setDataPagamento(dataPagamento);
		titulo.setFormaPagamentoQuitacao(form.getFormaPagamento());
		titulo.setStatus(StatusParcela.PAGO);
		if (form.getObservacao() != null && !form.getObservacao().isBlank()) {
			String atual = titulo.getObservacao() != null ? titulo.getObservacao() + " | " : "";
			titulo.setObservacao((atual + form.getObservacao()).trim());
		}
		TituloAPagar salvo = tituloRepository.save(titulo);
		logFinanceiroService.registrarTitulo("QUITACAO_PAGAR", salvo, valorAntes, salvo.getValorPago(),
				"Título " + salvo.getNumeroParcela() + "/" + salvo.getTotalParcelas()
						+ " quitado em " + dataPagamento + " via " + form.getFormaPagamento());
		return salvo;
	}

	@Transactional
	public TituloAPagar recalcularJurosTitulo(Long tituloId) {
		TituloAPagar titulo = consultarPorId(tituloId);
		if (titulo.getStatus() == StatusParcela.PAGO) {
			return titulo;
		}
		ConfiguracaoFinanceira config = configuracaoFinanceiraService.obterOuCriarPadrao();
		BigDecimal antes = titulo.getValorTotalAtualizado();
		calculoFinanceiroService.calcularEncargos(titulo, config, LocalDate.now());
		titulo.setJurosAtualizadosEm(LocalDateTime.now());
		if (titulo.getDataVencimento().isBefore(LocalDate.now()) && titulo.getStatus() == StatusParcela.PENDENTE) {
			titulo.setStatus(StatusParcela.VENCIDO);
		}
		TituloAPagar salvo = tituloRepository.save(titulo);
		logFinanceiroService.registrarTitulo("RECALCULO_JUROS_PAGAR", salvo, antes, salvo.getValorTotalAtualizado(),
				"Recálculo manual de encargos");
		return salvo;
	}

	@Transactional
	public int recalcularJurosFornecedor(Long fornecedorId) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		List<TituloAPagar> titulos = tituloRepository.findAllByEmpresaIdAndFornecedorIdAndStatusIn(empresaId, fornecedorId,
				List.of(StatusParcela.PENDENTE, StatusParcela.VENCIDO));
		ConfiguracaoFinanceira config = configuracaoFinanceiraService.obterOuCriarPadrao();
		int atualizados = 0;
		for (TituloAPagar t : titulos) {
			BigDecimal antes = t.getValorTotalAtualizado();
			calculoFinanceiroService.calcularEncargos(t, config, LocalDate.now());
			t.setJurosAtualizadosEm(LocalDateTime.now());
			if (t.getDataVencimento().isBefore(LocalDate.now()) && t.getStatus() == StatusParcela.PENDENTE) {
				t.setStatus(StatusParcela.VENCIDO);
			}
			tituloRepository.save(t);
			logFinanceiroService.registrarTitulo("RECALCULO_JUROS_PAGAR", t, antes, t.getValorTotalAtualizado(),
					"Recálculo em lote para fornecedor");
			atualizados++;
		}
		return atualizados;
	}

	@Transactional
	public int processarInadimplenciaGlobal() {
		LocalDate hoje = LocalDate.now();
		List<TituloAPagar> titulos = tituloRepository.findGlobalByStatusInAndDataVencimentoLessThan(
				List.of(StatusParcela.PENDENTE, StatusParcela.VENCIDO), hoje);
		int atualizados = 0;
		for (TituloAPagar t : titulos) {
			ConfiguracaoFinanceira config = configuracaoFinanceiraService.obterParaEmpresa(
					t.getEmpresa().getId(), t.getEmpresa());
			BigDecimal antes = t.getValorTotalAtualizado();
			calculoFinanceiroService.calcularEncargos(t, config, hoje);
			if (t.getStatus() == StatusParcela.PENDENTE) {
				t.setStatus(StatusParcela.VENCIDO);
			}
			t.setJurosAtualizadosEm(LocalDateTime.now());
			tituloRepository.save(t);
			logFinanceiroService.registrarTitulo("JOB_INADIMPLENCIA_PAGAR", t, antes, t.getValorTotalAtualizado(),
					"Atualização automática diária");
			atualizados++;
		}
		return atualizados;
	}

	private void validarForm(TituloForm form) throws Exception {
		if (form == null) {
			throw new Exception("Dados inválidos!");
		}
		if (form.getDescricao() == null || form.getDescricao().isBlank()) {
			throw new Exception("Informe a descrição!");
		}
		if (form.getValorTotal() == null || form.getValorTotal().signum() <= 0) {
			throw new Exception("Valor total inválido!");
		}
		if (form.getDataPrimeiroVencimento() == null) {
			throw new Exception("Informe a data de vencimento!");
		}
		if (form.getTotalParcelas() != null && form.getTotalParcelas() < 1) {
			throw new Exception("Quantidade de parcelas deve ser >= 1.");
		}
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

	public static class TituloForm {
		private Long fornecedorId;
		private CategoriaDespesa categoriaDespesa;
		private String descricao;
		private String documento;
		private BigDecimal valorTotal;
		private Integer totalParcelas;
		private LocalDate dataEmissao;
		private LocalDate dataPrimeiroVencimento;
		private String observacao;

		public Long getFornecedorId() { return fornecedorId; }
		public void setFornecedorId(Long fornecedorId) { this.fornecedorId = fornecedorId; }
		public CategoriaDespesa getCategoriaDespesa() { return categoriaDespesa; }
		public void setCategoriaDespesa(CategoriaDespesa categoriaDespesa) { this.categoriaDespesa = categoriaDespesa; }
		public String getDescricao() { return descricao; }
		public void setDescricao(String descricao) { this.descricao = descricao; }
		public String getDocumento() { return documento; }
		public void setDocumento(String documento) { this.documento = documento; }
		public BigDecimal getValorTotal() { return valorTotal; }
		public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
		public Integer getTotalParcelas() { return totalParcelas; }
		public void setTotalParcelas(Integer totalParcelas) { this.totalParcelas = totalParcelas; }
		public LocalDate getDataEmissao() { return dataEmissao; }
		public void setDataEmissao(LocalDate dataEmissao) { this.dataEmissao = dataEmissao; }
		public LocalDate getDataPrimeiroVencimento() { return dataPrimeiroVencimento; }
		public void setDataPrimeiroVencimento(LocalDate dataPrimeiroVencimento) { this.dataPrimeiroVencimento = dataPrimeiroVencimento; }
		public String getObservacao() { return observacao; }
		public void setObservacao(String observacao) { this.observacao = observacao; }
	}

	public static class QuitacaoTituloForm {
		private Long tituloId;
		private BigDecimal valorPago;
		private LocalDate dataPagamento;
		private FormaPagamento formaPagamento;
		private String observacao;

		public Long getTituloId() { return tituloId; }
		public void setTituloId(Long tituloId) { this.tituloId = tituloId; }
		public BigDecimal getValorPago() { return valorPago; }
		public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }
		public LocalDate getDataPagamento() { return dataPagamento; }
		public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
		public FormaPagamento getFormaPagamento() { return formaPagamento; }
		public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
		public String getObservacao() { return observacao; }
		public void setObservacao(String observacao) { this.observacao = observacao; }
	}

	public static class ResumoFornecedorCredor {
		private Long fornecedorId;
		private String nomeFornecedor;
		private BigDecimal totalAberto;
		private Integer qtdVencidas;
		private LocalDate proximoVencimento;

		public Long getFornecedorId() { return fornecedorId; }
		public void setFornecedorId(Long fornecedorId) { this.fornecedorId = fornecedorId; }
		public String getNomeFornecedor() { return nomeFornecedor; }
		public void setNomeFornecedor(String nomeFornecedor) { this.nomeFornecedor = nomeFornecedor; }
		public BigDecimal getTotalAberto() { return totalAberto; }
		public void setTotalAberto(BigDecimal totalAberto) { this.totalAberto = totalAberto; }
		public Integer getQtdVencidas() { return qtdVencidas; }
		public void setQtdVencidas(Integer qtdVencidas) { this.qtdVencidas = qtdVencidas; }
		public LocalDate getProximoVencimento() { return proximoVencimento; }
		public void setProximoVencimento(LocalDate proximoVencimento) { this.proximoVencimento = proximoVencimento; }
	}

	public static class IndicadoresContasAPagar {
		private BigDecimal totalAPagar = BigDecimal.ZERO;
		private BigDecimal totalVencido = BigDecimal.ZERO;
		private BigDecimal pagamentosMes = BigDecimal.ZERO;
		private Long fornecedoresEmAtraso = 0L;
		private LocalDateTime ultimaAtualizacaoJuros;

		public BigDecimal getTotalAPagar() { return totalAPagar; }
		public void setTotalAPagar(BigDecimal totalAPagar) { this.totalAPagar = totalAPagar; }
		public BigDecimal getTotalVencido() { return totalVencido; }
		public void setTotalVencido(BigDecimal totalVencido) { this.totalVencido = totalVencido; }
		public BigDecimal getPagamentosMes() { return pagamentosMes; }
		public void setPagamentosMes(BigDecimal pagamentosMes) { this.pagamentosMes = pagamentosMes; }
		public Long getFornecedoresEmAtraso() { return fornecedoresEmAtraso; }
		public void setFornecedoresEmAtraso(Long fornecedoresEmAtraso) { this.fornecedoresEmAtraso = fornecedoresEmAtraso; }
		public LocalDateTime getUltimaAtualizacaoJuros() { return ultimaAtualizacaoJuros; }
		public void setUltimaAtualizacaoJuros(LocalDateTime ultimaAtualizacaoJuros) { this.ultimaAtualizacaoJuros = ultimaAtualizacaoJuros; }
	}

}
