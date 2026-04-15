package br.com.gestao.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.gestao.entity.Cliente;
import br.com.gestao.entity.Estoque;
import br.com.gestao.entity.Produto;
import br.com.gestao.entity.Venda;
import br.com.gestao.entity.VendaItem;
import br.com.gestao.entity.VendaPagamento;
import br.com.gestao.entity.enums.FormaPagamento;
import br.com.gestao.entity.enums.StatusVenda;
import br.com.gestao.repository.ClienteRepository;
import br.com.gestao.repository.EstoqueRepository;
import br.com.gestao.repository.ProdutoRepository;
import br.com.gestao.repository.VendaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class VendaService {

	private final VendaRepository vendaRepository;
	private final ProdutoRepository produtoRepository;
	private final EstoqueRepository estoqueRepository;
	private final ClienteRepository clienteRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public VendaService(VendaRepository vendaRepository, ProdutoRepository produtoRepository,
			EstoqueRepository estoqueRepository, ClienteRepository clienteRepository,
			ContextoUsuarioService contextoUsuarioService) {
		this.vendaRepository = vendaRepository;
		this.produtoRepository = produtoRepository;
		this.estoqueRepository = estoqueRepository;
		this.clienteRepository = clienteRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public List<Venda> consultarTodas() {
		return vendaRepository.findAllByEmpresaIdOrderByDataVendaDesc(
				contextoUsuarioService.getEmpresaIdObrigatoria());
	}

	public List<Venda> consultarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		LocalDateTime inicio = dataInicio.atStartOfDay();
		LocalDateTime fim = dataFim.atTime(LocalTime.MAX);
		return vendaRepository.findAllByEmpresaIdAndDataVendaBetweenOrderByDataVendaDesc(empresaId, inicio, fim);
	}

	public Venda consultarPorId(Long id) {
		return vendaRepository.findByIdAndEmpresaId(id, contextoUsuarioService.getEmpresaIdObrigatoria())
				.orElseThrow(() -> new EntityNotFoundException("Venda nao encontrada!"));
	}

	@Transactional
	public Venda abrirVenda(Long clienteId) throws Exception {
		Venda venda = new Venda();
		venda.setEmpresa(contextoUsuarioService.getEmpresaObrigatoria());
		venda.setStatus(StatusVenda.ABERTA);
		venda.setDataVenda(LocalDateTime.now());

		if (clienteId != null) {
			Cliente cliente = clienteRepository
					.findByIdAndEmpresaId(clienteId, contextoUsuarioService.getEmpresaIdObrigatoria())
					.orElseThrow(() -> new Exception("Cliente nao encontrado!"));
			venda.setCliente(cliente);
		}

		return vendaRepository.save(venda);
	}

	@Transactional
	public Venda adicionarItem(Long vendaId, Long produtoId, Integer quantidade, BigDecimal desconto) throws Exception {
		Venda venda = consultarPorId(vendaId);
		validarVendaAberta(venda);

		if (produtoId == null) {
			throw new Exception("Produto nao informado!");
		}
		if (quantidade == null || quantidade <= 0) {
			throw new Exception("Quantidade invalida!");
		}

		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		Produto produto = produtoRepository.findByIdAndEmpresaId(produtoId, empresaId)
				.orElseThrow(() -> new Exception("Produto nao encontrado!"));

		if (!produto.isAtivo()) {
			throw new Exception("Produto inativo!");
		}

		Estoque estoque = estoqueRepository.findByProdutoId(produtoId)
				.orElseThrow(() -> new Exception("Estoque nao encontrado para o produto!"));

		if (estoque.getQuantidade() < quantidade) {
			throw new Exception("Estoque insuficiente! Disponivel: " + estoque.getQuantidade());
		}

		VendaItem item = new VendaItem();
		item.setProduto(produto);
		item.setQuantidade(quantidade);
		item.setPrecoUnitario(produto.getPreco());
		item.setDesconto(desconto != null ? desconto : BigDecimal.ZERO);
		item.calcularSubtotal();

		venda.adicionarItem(item);
		venda.recalcularTotais();

		return vendaRepository.save(venda);
	}

	@Transactional
	public Venda removerItem(Long vendaId, Long itemId) throws Exception {
		Venda venda = consultarPorId(vendaId);
		validarVendaAberta(venda);

		VendaItem item = venda.getItens().stream()
				.filter(i -> i.getId().equals(itemId))
				.findFirst()
				.orElseThrow(() -> new Exception("Item nao encontrado na venda!"));

		venda.getItens().remove(item);
		venda.recalcularTotais();

		return vendaRepository.save(venda);
	}

	@Transactional
	public Venda aplicarDesconto(Long vendaId, BigDecimal desconto) throws Exception {
		Venda venda = consultarPorId(vendaId);
		validarVendaAberta(venda);

		if (desconto == null || desconto.compareTo(BigDecimal.ZERO) < 0) {
			throw new Exception("Desconto invalido!");
		}
		if (desconto.compareTo(venda.getSubtotal()) > 0) {
			throw new Exception("Desconto nao pode ser maior que o subtotal!");
		}

		venda.setDesconto(desconto);
		venda.recalcularTotais();

		return vendaRepository.save(venda);
	}

	@Transactional
	public Venda adicionarPagamento(Long vendaId, FormaPagamento formaPagamento, BigDecimal valor) throws Exception {
		Venda venda = consultarPorId(vendaId);
		validarVendaAberta(venda);

		if (formaPagamento == null) {
			throw new Exception("Forma de pagamento nao informada!");
		}
		if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
			throw new Exception("Valor do pagamento invalido!");
		}

		BigDecimal totalPago = venda.getTotalPago();
		BigDecimal restante = venda.getValorTotal().subtract(totalPago);

		if (valor.compareTo(restante) > 0) {
			throw new Exception("Valor do pagamento excede o restante! Restante: R$ " + restante);
		}

		VendaPagamento pagamento = new VendaPagamento();
		pagamento.setFormaPagamento(formaPagamento);
		pagamento.setValor(valor);

		venda.adicionarPagamento(pagamento);

		return vendaRepository.save(venda);
	}

	@Transactional
	public Venda removerPagamento(Long vendaId, Long pagamentoId) throws Exception {
		Venda venda = consultarPorId(vendaId);
		validarVendaAberta(venda);

		VendaPagamento pagamento = venda.getPagamentos().stream()
				.filter(p -> p.getId().equals(pagamentoId))
				.findFirst()
				.orElseThrow(() -> new Exception("Pagamento nao encontrado!"));

		venda.getPagamentos().remove(pagamento);

		return vendaRepository.save(venda);
	}

	@Transactional
	public Venda finalizarVenda(Long vendaId) throws Exception {
		Venda venda = consultarPorId(vendaId);
		validarVendaAberta(venda);

		if (venda.getItens().isEmpty()) {
			throw new Exception("A venda nao possui itens!");
		}

		BigDecimal totalPago = venda.getTotalPago();
		if (totalPago.compareTo(venda.getValorTotal()) < 0) {
			throw new Exception("Pagamento incompleto! Falta: R$ "
					+ venda.getValorTotal().subtract(totalPago));
		}

		// Baixa no estoque
		for (VendaItem item : venda.getItens()) {
			Estoque estoque = estoqueRepository.findByProdutoId(item.getProduto().getId())
					.orElseThrow(() -> new Exception(
							"Estoque nao encontrado para o produto: " + item.getProduto().getDescricao()));

			int novaQuantidade = estoque.getQuantidade() - item.getQuantidade();
			if (novaQuantidade < 0) {
				throw new Exception("Estoque insuficiente para o produto: " + item.getProduto().getDescricao()
						+ ". Disponivel: " + estoque.getQuantidade());
			}
			estoque.setQuantidade(novaQuantidade);
			estoqueRepository.save(estoque);
		}

		venda.setStatus(StatusVenda.FINALIZADA);
		return vendaRepository.save(venda);
	}

	@Transactional
	public Venda cancelarVenda(Long vendaId) throws Exception {
		Venda venda = consultarPorId(vendaId);

		if (venda.getStatus() == StatusVenda.CANCELADA) {
			throw new Exception("Venda ja esta cancelada!");
		}

		// Se ja foi finalizada, devolver estoque
		if (venda.getStatus() == StatusVenda.FINALIZADA) {
			for (VendaItem item : venda.getItens()) {
				Estoque estoque = estoqueRepository.findByProdutoId(item.getProduto().getId())
						.orElse(null);
				if (estoque != null) {
					estoque.setQuantidade(estoque.getQuantidade() + item.getQuantidade());
					estoqueRepository.save(estoque);
				}
			}
		}

		venda.setStatus(StatusVenda.CANCELADA);
		return vendaRepository.save(venda);
	}

	private void validarVendaAberta(Venda venda) throws Exception {
		if (venda.getStatus() != StatusVenda.ABERTA) {
			throw new Exception("Esta venda nao esta aberta! Status: " + venda.getStatus().getDescricao());
		}
	}

}
