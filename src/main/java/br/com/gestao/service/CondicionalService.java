package br.com.gestao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestao.entity.Cliente;
import br.com.gestao.entity.Condicional;
import br.com.gestao.entity.CondicionalItem;
import br.com.gestao.entity.Empresa;
import br.com.gestao.entity.Produto;
import br.com.gestao.entity.Venda;
import br.com.gestao.entity.VendaItem;
import br.com.gestao.entity.enums.StatusCondicional;
import br.com.gestao.entity.enums.StatusItemCondicional;
import br.com.gestao.entity.enums.StatusVenda;
import br.com.gestao.repository.ClienteRepository;
import br.com.gestao.repository.CondicionalRepository;
import br.com.gestao.repository.ProdutoRepository;
import br.com.gestao.repository.VendaRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class CondicionalService {

	public static final int PRAZO_PADRAO_DIAS = 7;

	private static final ZoneId ZONE_SP = ZoneId.of("America/Sao_Paulo");

	private final CondicionalRepository condicionalRepository;
	private final ClienteRepository clienteRepository;
	private final ProdutoRepository produtoRepository;
	private final VendaRepository vendaRepository;
	private final EstoqueService estoqueService;
	private final ContextoUsuarioService contextoUsuarioService;

	public CondicionalService(CondicionalRepository condicionalRepository,
			ClienteRepository clienteRepository,
			ProdutoRepository produtoRepository,
			VendaRepository vendaRepository,
			EstoqueService estoqueService,
			ContextoUsuarioService contextoUsuarioService) {
		this.condicionalRepository = condicionalRepository;
		this.clienteRepository = clienteRepository;
		this.produtoRepository = produtoRepository;
		this.vendaRepository = vendaRepository;
		this.estoqueService = estoqueService;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public IndicadoresCondicional calcularIndicadores() {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		IndicadoresCondicional ind = new IndicadoresCondicional();
		List<Condicional> abertas = condicionalRepository.findAllByEmpresaIdAndStatusOrderByDataSaidaDesc(
				empresaId, StatusCondicional.ABERTA);
		List<Condicional> parciais = condicionalRepository.findAllByEmpresaIdAndStatusOrderByDataSaidaDesc(
				empresaId, StatusCondicional.PARCIALMENTE_DEVOLVIDA);
		List<Condicional> vencidas = condicionalRepository.findAllByEmpresaIdAndStatusOrderByDataSaidaDesc(
				empresaId, StatusCondicional.VENCIDA);

		ind.setTotalAbertas(abertas.size() + parciais.size());
		ind.setTotalVencidas(vencidas.size());

		BigDecimal valorEmPoder = BigDecimal.ZERO;
		List<Condicional> emPoder = new ArrayList<>();
		emPoder.addAll(abertas);
		emPoder.addAll(parciais);
		emPoder.addAll(vencidas);
		for (Condicional c : emPoder) {
			for (CondicionalItem item : c.getItens()) {
				int pendente = item.getQuantidadePendente();
				if (pendente > 0) {
					valorEmPoder = valorEmPoder.add(
							item.getPrecoUnitario().multiply(BigDecimal.valueOf(pendente)));
				}
			}
		}
		ind.setValorEmPoderClientes(valorEmPoder.setScale(2, RoundingMode.HALF_UP));
		return ind;
	}

	public List<Condicional> listar() {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		return condicionalRepository.findAllByEmpresaIdOrderByDataSaidaDesc(empresaId);
	}

	public List<Condicional> filtrar(StatusCondicional status, Long clienteId, LocalDate inicio, LocalDate fim) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		LocalDateTime inicioDh = inicio != null ? inicio.atStartOfDay() : null;
		LocalDateTime fimDh = fim != null ? fim.atTime(23, 59, 59) : null;
		return condicionalRepository.filtrar(empresaId, status, clienteId, inicioDh, fimDh);
	}

	public Condicional consultarPorId(Long id) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		return condicionalRepository.findByIdAndEmpresaId(id, empresaId)
				.orElseThrow(() -> new EntityNotFoundException("Condicional não encontrada!"));
	}

	@Transactional
	public Condicional abrir(CondicionalForm form) throws Exception {
		validarForm(form);
		Empresa empresa = contextoUsuarioService.getEmpresaLogada();
		Cliente cliente = clienteRepository.findByIdAndEmpresaId(form.getClienteId(), empresa.getId())
				.orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado!"));

		Condicional condicional = new Condicional();
		condicional.setEmpresa(empresa);
		condicional.setCliente(cliente);
		condicional.setDataSaida(LocalDateTime.now(ZONE_SP));
		condicional.setDataPrevistaDevolucao(form.getDataPrevistaDevolucao());
		condicional.setObservacao(form.getObservacao());
		condicional.setStatus(StatusCondicional.ABERTA);

		for (CondicionalItemForm itemForm : form.getItens()) {
			Produto produto = produtoRepository.findByIdAndEmpresaId(itemForm.getProdutoId(), empresa.getId())
					.orElseThrow(() -> new EntityNotFoundException(
							"Produto " + itemForm.getProdutoId() + " não encontrado!"));

			CondicionalItem item = new CondicionalItem();
			item.setProduto(produto);
			item.setQuantidade(itemForm.getQuantidade());
			item.setQuantidadeDevolvida(0);
			item.setQuantidadeConvertida(0);
			BigDecimal preco = itemForm.getPrecoUnitario() != null
					? itemForm.getPrecoUnitario()
					: produto.getPreco();
			if (preco == null) {
				preco = BigDecimal.ZERO;
			}
			item.setPrecoUnitario(preco.setScale(2, RoundingMode.HALF_UP));
			item.setStatus(StatusItemCondicional.EM_PODER_CLIENTE);
			condicional.adicionarItem(item);
		}

		// Baixa de estoque — EstoqueService.ajustar lança exceção se não houver saldo
		for (CondicionalItem item : condicional.getItens()) {
			estoqueService.ajustar(item.getProduto().getId(), -item.getQuantidade(),
					"CONDICIONAL_SAIDA");
		}

		return condicionalRepository.save(condicional);
	}

	@Transactional
	public Condicional cancelar(Long id) throws Exception {
		Condicional condicional = consultarPorId(id);
		if (condicional.getStatus() == StatusCondicional.FINALIZADA
				|| condicional.getStatus() == StatusCondicional.CANCELADA) {
			throw new Exception("Condicional já finalizada ou cancelada não pode ser cancelada.");
		}
		for (CondicionalItem item : condicional.getItens()) {
			if (item.getQuantidadeConvertida() != null && item.getQuantidadeConvertida() > 0) {
				throw new Exception("Não é possível cancelar: existem itens já convertidos em venda.");
			}
		}

		LocalDateTime agora = LocalDateTime.now(ZONE_SP);
		for (CondicionalItem item : condicional.getItens()) {
			int pendente = item.getQuantidadePendente();
			if (pendente > 0) {
				estoqueService.ajustar(item.getProduto().getId(), pendente, "CONDICIONAL_CANCELAMENTO");
				item.setQuantidadeDevolvida(
						(item.getQuantidadeDevolvida() != null ? item.getQuantidadeDevolvida() : 0) + pendente);
				item.setDataDevolucao(agora);
			}
			if (item.getStatus() != StatusItemCondicional.CONVERTIDO_VENDA) {
				item.setStatus(StatusItemCondicional.DEVOLVIDO);
			}
		}
		condicional.setStatus(StatusCondicional.CANCELADA);
		condicional.setDataFechamento(agora);
		return condicionalRepository.save(condicional);
	}

	@Transactional
	public Condicional devolverItens(Long condicionalId, List<MovimentoItemForm> devolucoes) throws Exception {
		if (devolucoes == null || devolucoes.isEmpty()) {
			throw new Exception("Informe ao menos um item para devolução.");
		}
		Condicional condicional = consultarPorId(condicionalId);
		validarMovimentavel(condicional);

		LocalDateTime agora = LocalDateTime.now(ZONE_SP);
		for (MovimentoItemForm mov : devolucoes) {
			CondicionalItem item = encontrarItem(condicional, mov.getCondicionalItemId());
			int qtd = mov.getQuantidade() != null ? mov.getQuantidade() : 0;
			if (qtd <= 0) {
				continue;
			}
			int pendente = item.getQuantidadePendente();
			if (qtd > pendente) {
				throw new Exception("Quantidade a devolver (" + qtd + ") maior que a pendente (" + pendente
						+ ") no item " + item.getProduto().getDescricao() + ".");
			}
			estoqueService.ajustar(item.getProduto().getId(), qtd, "CONDICIONAL_DEVOLUCAO");
			item.setQuantidadeDevolvida(nz(item.getQuantidadeDevolvida()) + qtd);
			item.setDataDevolucao(agora);
			recalcularStatusItem(item);
		}

		recalcularStatusCondicional(condicional, agora);
		return condicionalRepository.save(condicional);
	}

	@Transactional
	public Venda converterEmVenda(Long condicionalId, List<MovimentoItemForm> conversoes) throws Exception {
		if (conversoes == null || conversoes.isEmpty()) {
			throw new Exception("Informe ao menos um item para conversão.");
		}
		Condicional condicional = consultarPorId(condicionalId);
		validarMovimentavel(condicional);

		LocalDateTime agora = LocalDateTime.now(ZONE_SP);

		Venda venda = condicional.getVendaGerada();
		boolean vendaNova = false;
		if (venda == null) {
			venda = new Venda();
			venda.setEmpresa(condicional.getEmpresa());
			venda.setCliente(condicional.getCliente());
			venda.setStatus(StatusVenda.ABERTA);
			venda.setDataVenda(agora);
			vendaNova = true;
		} else if (venda.getStatus() != StatusVenda.ABERTA) {
			throw new Exception("A venda gerada para esta condicional já foi finalizada/cancelada e não aceita "
					+ "novas conversões.");
		}

		for (MovimentoItemForm mov : conversoes) {
			CondicionalItem item = encontrarItem(condicional, mov.getCondicionalItemId());
			int qtd = mov.getQuantidade() != null ? mov.getQuantidade() : 0;
			if (qtd <= 0) {
				continue;
			}
			int pendente = item.getQuantidadePendente();
			if (qtd > pendente) {
				throw new Exception("Quantidade a converter (" + qtd + ") maior que a pendente (" + pendente
						+ ") no item " + item.getProduto().getDescricao() + ".");
			}
			BigDecimal preco = mov.getPrecoUnitarioOverride() != null
					? mov.getPrecoUnitarioOverride().setScale(2, RoundingMode.HALF_UP)
					: item.getPrecoUnitario();

			// Devolve a quantidade ao estoque para que a Venda siga o fluxo normal
			// (a baixa definitiva ocorre quando o usuário finalizar a venda).
			estoqueService.ajustar(item.getProduto().getId(), qtd, "CONDICIONAL_CONVERSAO_RETORNO");

			VendaItem vi = new VendaItem();
			vi.setProduto(item.getProduto());
			vi.setQuantidade(qtd);
			vi.setPrecoUnitario(preco);
			vi.setDesconto(BigDecimal.ZERO);
			vi.calcularSubtotal();
			venda.adicionarItem(vi);

			item.setQuantidadeConvertida(nz(item.getQuantidadeConvertida()) + qtd);
			item.setDataConversao(agora);
			recalcularStatusItem(item);
		}

		venda.recalcularTotais();
		Venda salva = vendaRepository.save(venda);

		// Vincula vendaItem gerado a cada CondicionalItem convertido nesta chamada.
		// Como podem existir múltiplas conversões na mesma venda, preserva o vínculo
		// já existente; sobrescreve apenas quando ainda nulo.
		for (MovimentoItemForm mov : conversoes) {
			if (mov.getQuantidade() == null || mov.getQuantidade() <= 0) {
				continue;
			}
			CondicionalItem item = encontrarItem(condicional, mov.getCondicionalItemId());
			if (item.getVendaItemGerado() == null) {
				salva.getItens().stream()
						.filter(vi -> vi.getProduto().getId().equals(item.getProduto().getId())
								&& vi.getId() != null)
						.reduce((a, b) -> b)
						.ifPresent(item::setVendaItemGerado);
			}
		}

		if (vendaNova) {
			condicional.setVendaGerada(salva);
		}
		recalcularStatusCondicional(condicional, agora);
		condicionalRepository.save(condicional);
		return salva;
	}

	private CondicionalItem encontrarItem(Condicional condicional, Long itemId) throws Exception {
		if (itemId == null) {
			throw new Exception("Item da condicional não informado.");
		}
		return condicional.getItens().stream()
				.filter(i -> itemId.equals(i.getId()))
				.findFirst()
				.orElseThrow(() -> new EntityNotFoundException(
						"Item " + itemId + " não pertence à condicional " + condicional.getId() + "."));
	}

	private void validarMovimentavel(Condicional condicional) throws Exception {
		StatusCondicional s = condicional.getStatus();
		if (s == StatusCondicional.FINALIZADA || s == StatusCondicional.CANCELADA) {
			throw new Exception("Condicional " + s.getDescricao().toLowerCase() + " não aceita movimentações.");
		}
	}

	private void recalcularStatusItem(CondicionalItem item) {
		int total = nz(item.getQuantidade());
		int dev = nz(item.getQuantidadeDevolvida());
		int conv = nz(item.getQuantidadeConvertida());
		int pendente = total - dev - conv;

		if (pendente == total) {
			item.setStatus(StatusItemCondicional.EM_PODER_CLIENTE);
		} else if (pendente == 0 && conv == total) {
			item.setStatus(StatusItemCondicional.CONVERTIDO_VENDA);
		} else if (pendente == 0 && dev == total) {
			item.setStatus(StatusItemCondicional.DEVOLVIDO);
		} else {
			// pendente > 0 com algum movimento, ou pendente == 0 com mix dev+conv
			item.setStatus(StatusItemCondicional.PARCIALMENTE_RESOLVIDO);
		}
	}

	private void recalcularStatusCondicional(Condicional condicional, LocalDateTime agora) {
		boolean tudoResolvido = true;
		boolean algumMovimento = false;
		for (CondicionalItem i : condicional.getItens()) {
			int dev = nz(i.getQuantidadeDevolvida());
			int conv = nz(i.getQuantidadeConvertida());
			if (dev > 0 || conv > 0) {
				algumMovimento = true;
			}
			if (i.getQuantidadePendente() > 0) {
				tudoResolvido = false;
			}
		}
		if (tudoResolvido) {
			condicional.setStatus(StatusCondicional.FINALIZADA);
			if (condicional.getDataFechamento() == null) {
				condicional.setDataFechamento(agora);
			}
		} else if (algumMovimento) {
			condicional.setStatus(StatusCondicional.PARCIALMENTE_DEVOLVIDA);
		}
		// caso ainda esteja sem movimento mantém o status atual (ABERTA ou VENCIDA)
	}

	private int nz(Integer v) {
		return v != null ? v : 0;
	}

	private void validarForm(CondicionalForm form) throws Exception {
		if (form == null) {
			throw new Exception("Dados inválidos!");
		}
		if (form.getClienteId() == null) {
			throw new Exception("Informe o cliente!");
		}
		if (form.getDataPrevistaDevolucao() == null) {
			throw new Exception("Informe a data prevista de devolução!");
		}
		if (form.getDataPrevistaDevolucao().isBefore(LocalDate.now(ZONE_SP))) {
			throw new Exception("Data prevista de devolução não pode estar no passado.");
		}
		if (form.getItens() == null || form.getItens().isEmpty()) {
			throw new Exception("Adicione ao menos um item!");
		}
		for (CondicionalItemForm item : form.getItens()) {
			if (item.getProdutoId() == null) {
				throw new Exception("Produto inválido em um dos itens.");
			}
			if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
				throw new Exception("Quantidade inválida para um dos itens.");
			}
			if (item.getPrecoUnitario() != null && item.getPrecoUnitario().signum() < 0) {
				throw new Exception("Preço unitário não pode ser negativo.");
			}
		}
	}

	// ------ Form DTOs / View Models ------

	public static class CondicionalForm {
		private Long clienteId;
		private LocalDate dataPrevistaDevolucao;
		private String observacao;
		private List<CondicionalItemForm> itens = new ArrayList<>();

		public Long getClienteId() { return clienteId; }
		public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
		public LocalDate getDataPrevistaDevolucao() { return dataPrevistaDevolucao; }
		public void setDataPrevistaDevolucao(LocalDate dataPrevistaDevolucao) { this.dataPrevistaDevolucao = dataPrevistaDevolucao; }
		public String getObservacao() { return observacao; }
		public void setObservacao(String observacao) { this.observacao = observacao; }
		public List<CondicionalItemForm> getItens() { return itens; }
		public void setItens(List<CondicionalItemForm> itens) { this.itens = itens; }
	}

	public static class CondicionalItemForm {
		private Long produtoId;
		private Integer quantidade;
		private BigDecimal precoUnitario;

		public Long getProdutoId() { return produtoId; }
		public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
		public Integer getQuantidade() { return quantidade; }
		public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
		public BigDecimal getPrecoUnitario() { return precoUnitario; }
		public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }
	}

	public static class MovimentoItemForm {
		private Long condicionalItemId;
		private Integer quantidade;
		private BigDecimal precoUnitarioOverride;

		public Long getCondicionalItemId() { return condicionalItemId; }
		public void setCondicionalItemId(Long condicionalItemId) { this.condicionalItemId = condicionalItemId; }
		public Integer getQuantidade() { return quantidade; }
		public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
		public BigDecimal getPrecoUnitarioOverride() { return precoUnitarioOverride; }
		public void setPrecoUnitarioOverride(BigDecimal precoUnitarioOverride) { this.precoUnitarioOverride = precoUnitarioOverride; }
	}

	public static class IndicadoresCondicional {
		private Integer totalAbertas = 0;
		private Integer totalVencidas = 0;
		private BigDecimal valorEmPoderClientes = BigDecimal.ZERO;

		public Integer getTotalAbertas() { return totalAbertas; }
		public void setTotalAbertas(Integer totalAbertas) { this.totalAbertas = totalAbertas; }
		public Integer getTotalVencidas() { return totalVencidas; }
		public void setTotalVencidas(Integer totalVencidas) { this.totalVencidas = totalVencidas; }
		public BigDecimal getValorEmPoderClientes() { return valorEmPoderClientes; }
		public void setValorEmPoderClientes(BigDecimal valorEmPoderClientes) { this.valorEmPoderClientes = valorEmPoderClientes; }
	}

}
