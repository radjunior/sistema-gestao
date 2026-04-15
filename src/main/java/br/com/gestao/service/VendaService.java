package br.com.gestao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestao.entity.Cliente;
import br.com.gestao.entity.ConfiguracaoFinanceira;
import br.com.gestao.entity.Empresa;
import br.com.gestao.entity.FormaPagamento;
import br.com.gestao.entity.ItemVenda;
import br.com.gestao.entity.Parcela;
import br.com.gestao.entity.Produto;
import br.com.gestao.entity.StatusParcela;
import br.com.gestao.entity.StatusVenda;
import br.com.gestao.entity.Venda;
import br.com.gestao.repository.ClienteRepository;
import br.com.gestao.repository.ProdutoRepository;
import br.com.gestao.repository.VendaRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class VendaService {

	private final VendaRepository vendaRepository;
	private final ProdutoRepository produtoRepository;
	private final ClienteRepository clienteRepository;
	private final ConfiguracaoFinanceiraService configuracaoFinanceiraService;
	private final CalculoFinanceiroService calculoFinanceiroService;
	private final ContextoUsuarioService contextoUsuarioService;
	private final LogFinanceiroService logFinanceiroService;

	public VendaService(VendaRepository vendaRepository, ProdutoRepository produtoRepository,
			ClienteRepository clienteRepository, ConfiguracaoFinanceiraService configuracaoFinanceiraService,
			CalculoFinanceiroService calculoFinanceiroService, ContextoUsuarioService contextoUsuarioService,
			LogFinanceiroService logFinanceiroService) {
		this.vendaRepository = vendaRepository;
		this.produtoRepository = produtoRepository;
		this.clienteRepository = clienteRepository;
		this.configuracaoFinanceiraService = configuracaoFinanceiraService;
		this.calculoFinanceiroService = calculoFinanceiroService;
		this.contextoUsuarioService = contextoUsuarioService;
		this.logFinanceiroService = logFinanceiroService;
	}

	public List<Venda> consultarRecentes() {
		return vendaRepository.findAllByEmpresaIdOrderByDataVendaDesc(contextoUsuarioService.getEmpresaIdObrigatoria());
	}

	public Venda consultarPorId(Long id) {
		return vendaRepository.findByIdAndEmpresaId(id, contextoUsuarioService.getEmpresaIdObrigatoria())
				.orElseThrow(() -> new EntityNotFoundException("Venda nao encontrada!"));
	}

	@Transactional
	public Venda finalizar(VendaForm form) throws Exception {
		if (form == null) {
			throw new Exception("Dados da venda invalidos!");
		}
		if (form.getItens() == null || form.getItens().isEmpty()) {
			throw new Exception("Adicione pelo menos um item a venda!");
		}
		if (form.getFormaPagamento() == null) {
			throw new Exception("Forma de pagamento obrigatoria!");
		}

		Empresa empresa = contextoUsuarioService.getEmpresaObrigatoria();
		Long empresaId = empresa.getId();
		ConfiguracaoFinanceira config = configuracaoFinanceiraService.obterOuCriarPadrao();

		Venda venda = new Venda();
		venda.setEmpresa(empresa);
		venda.setDataVenda(LocalDateTime.now());
		venda.setFormaPagamento(form.getFormaPagamento());
		venda.setStatus(StatusVenda.FINALIZADA);
		venda.setObservacao(form.getObservacao());

		BigDecimal subtotal = BigDecimal.ZERO;
		List<ItemVenda> itens = new ArrayList<>();
		for (VendaItemForm linha : form.getItens()) {
			if (linha.getProdutoId() == null) {
				throw new Exception("Produto invalido em uma das linhas!");
			}
			Produto produto = produtoRepository.findByIdAndEmpresaId(linha.getProdutoId(), empresaId)
					.orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado: " + linha.getProdutoId()));
			BigDecimal qtd = linha.getQuantidade() != null ? linha.getQuantidade() : BigDecimal.ONE;
			if (qtd.signum() <= 0) {
				throw new Exception("Quantidade invalida para o produto " + produto.getDescricao());
			}
			BigDecimal preco = linha.getPrecoUnitario() != null ? linha.getPrecoUnitario() : produto.getPreco();
			if (preco == null || preco.signum() < 0) {
				throw new Exception("Preco unitario invalido para o produto " + produto.getDescricao());
			}
			BigDecimal valorLinha = preco.multiply(qtd).setScale(2, RoundingMode.HALF_UP);
			ItemVenda item = new ItemVenda();
			item.setVenda(venda);
			item.setProduto(produto);
			item.setDescricaoSnapshot(produto.getDescricao());
			item.setQuantidade(qtd);
			item.setPrecoUnitario(preco);
			item.setSubtotal(valorLinha);
			itens.add(item);
			subtotal = subtotal.add(valorLinha);
		}

		BigDecimal desconto = form.getValorDesconto() != null ? form.getValorDesconto() : BigDecimal.ZERO;
		if (desconto.signum() < 0) {
			throw new Exception("Desconto invalido!");
		}
		if (desconto.compareTo(subtotal) > 0) {
			throw new Exception("Desconto maior que o subtotal da venda!");
		}
		BigDecimal total = subtotal.subtract(desconto).setScale(2, RoundingMode.HALF_UP);
		venda.setValorSubtotal(subtotal);
		venda.setValorDesconto(desconto);
		venda.setValorTotal(total);
		venda.setItens(itens);

		boolean isParcelado = form.getFormaPagamento() == FormaPagamento.PARCELADO;
		int qtdParcelas = isParcelado ? (form.getTotalParcelas() != null ? form.getTotalParcelas() : 1) : 1;
		if (isParcelado) {
			if (qtdParcelas < 1) {
				throw new Exception("Quantidade de parcelas invalida!");
			}
			if (qtdParcelas > config.getMaxParcelas()) {
				throw new Exception("Quantidade de parcelas excede o maximo permitido (" + config.getMaxParcelas() + ")!");
			}
			if (form.getClienteId() == null) {
				throw new Exception("Selecione um cliente para vendas parceladas!");
			}
		}

		Cliente cliente = null;
		if (form.getClienteId() != null) {
			cliente = clienteRepository.findByIdAndEmpresaId(form.getClienteId(), empresaId)
					.orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado!"));
			venda.setCliente(cliente);
		}

		venda.setParcelado(isParcelado);
		venda.setTotalParcelas(qtdParcelas);
		venda.setComJuros(isParcelado && Boolean.TRUE.equals(form.getComJuros()));

		BigDecimal taxa = BigDecimal.ZERO;
		List<BigDecimal> valoresParcelas;
		if (isParcelado && venda.isComJuros()) {
			taxa = form.getTaxaJurosMensal() != null ? form.getTaxaJurosMensal() : config.getTaxaJurosParcelamento();
			valoresParcelas = calculoFinanceiroService.calcularParcelasComJuros(total, qtdParcelas, taxa);
		} else {
			valoresParcelas = calculoFinanceiroService.calcularParcelasSemJuros(total, qtdParcelas);
		}
		venda.setTaxaJurosMensal(taxa);
		BigDecimal totalComJuros = calculoFinanceiroService.somar(valoresParcelas);
		venda.setValorTotalComJuros(totalComJuros);

		if (isParcelado) {
			int dias = form.getDiasPrimeiraParcela() != null ? form.getDiasPrimeiraParcela() : config.getDiasPrimeiraParcela();
			LocalDate base = LocalDate.now();
			List<LocalDate> vencimentos = calculoFinanceiroService.calcularVencimentos(base, qtdParcelas, dias);
			List<Parcela> parcelas = new ArrayList<>(qtdParcelas);
			for (int i = 0; i < qtdParcelas; i++) {
				Parcela p = new Parcela();
				p.setEmpresa(empresa);
				p.setVenda(venda);
				p.setCliente(cliente);
				p.setNumeroParcela(i + 1);
				p.setTotalParcelas(qtdParcelas);
				p.setValorNominal(valoresParcelas.get(i));
				p.setDataVencimento(vencimentos.get(i));
				p.setStatus(StatusParcela.PENDENTE);
				parcelas.add(p);
			}
			venda.setParcelas(parcelas);
		}

		Venda salva = vendaRepository.save(venda);
		logFinanceiroService.registrar("VENDA_FINALIZADA", null, null, salva.getValorTotal(),
				"Venda #" + salva.getId() + " - " + form.getFormaPagamento()
						+ (isParcelado ? " em " + qtdParcelas + "x" : ""));
		return salva;
	}

	// ------ Form DTOs ------

	public static class VendaForm {
		private Long clienteId;
		private FormaPagamento formaPagamento;
		private Integer totalParcelas;
		private Boolean comJuros;
		private BigDecimal taxaJurosMensal;
		private Integer diasPrimeiraParcela;
		private BigDecimal valorDesconto;
		private String observacao;
		private List<VendaItemForm> itens = new ArrayList<>();

		public Long getClienteId() { return clienteId; }
		public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
		public FormaPagamento getFormaPagamento() { return formaPagamento; }
		public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
		public Integer getTotalParcelas() { return totalParcelas; }
		public void setTotalParcelas(Integer totalParcelas) { this.totalParcelas = totalParcelas; }
		public Boolean getComJuros() { return comJuros; }
		public void setComJuros(Boolean comJuros) { this.comJuros = comJuros; }
		public BigDecimal getTaxaJurosMensal() { return taxaJurosMensal; }
		public void setTaxaJurosMensal(BigDecimal taxaJurosMensal) { this.taxaJurosMensal = taxaJurosMensal; }
		public Integer getDiasPrimeiraParcela() { return diasPrimeiraParcela; }
		public void setDiasPrimeiraParcela(Integer diasPrimeiraParcela) { this.diasPrimeiraParcela = diasPrimeiraParcela; }
		public BigDecimal getValorDesconto() { return valorDesconto; }
		public void setValorDesconto(BigDecimal valorDesconto) { this.valorDesconto = valorDesconto; }
		public String getObservacao() { return observacao; }
		public void setObservacao(String observacao) { this.observacao = observacao; }
		public List<VendaItemForm> getItens() { return itens; }
		public void setItens(List<VendaItemForm> itens) { this.itens = itens; }
	}

	public static class VendaItemForm {
		private Long produtoId;
		private BigDecimal quantidade;
		private BigDecimal precoUnitario;

		public Long getProdutoId() { return produtoId; }
		public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
		public BigDecimal getQuantidade() { return quantidade; }
		public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
		public BigDecimal getPrecoUnitario() { return precoUnitario; }
		public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }
	}

}
