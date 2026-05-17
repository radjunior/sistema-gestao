package br.com.gestao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestao.entity.Devolucao;
import br.com.gestao.entity.DevolucaoItem;
import br.com.gestao.entity.Parcela;
import br.com.gestao.entity.Venda;
import br.com.gestao.entity.VendaItem;
import br.com.gestao.entity.enums.StatusParcela;
import br.com.gestao.entity.enums.StatusVenda;
import br.com.gestao.repository.DevolucaoItemRepository;
import br.com.gestao.repository.DevolucaoRepository;
import br.com.gestao.repository.VendaRepository;

@Service
public class DevolucaoService {

	private final DevolucaoRepository devolucaoRepository;
	private final DevolucaoItemRepository devolucaoItemRepository;
	private final VendaService vendaService;
	private final VendaRepository vendaRepository;
	private final EstoqueService estoqueService;
	private final LogFinanceiroService logFinanceiroService;
	private final ContextoUsuarioService contextoUsuarioService;

	public DevolucaoService(DevolucaoRepository devolucaoRepository,
			DevolucaoItemRepository devolucaoItemRepository, VendaService vendaService,
			VendaRepository vendaRepository, EstoqueService estoqueService,
			LogFinanceiroService logFinanceiroService, ContextoUsuarioService contextoUsuarioService) {
		this.devolucaoRepository = devolucaoRepository;
		this.devolucaoItemRepository = devolucaoItemRepository;
		this.vendaService = vendaService;
		this.vendaRepository = vendaRepository;
		this.estoqueService = estoqueService;
		this.logFinanceiroService = logFinanceiroService;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public record ItemDevolvivel(Long vendaItemId, Long produtoId, String descricao,
			int quantidadeVendida, int jaDevolvido, int disponivel, BigDecimal valorUnitario) {
	}

	public record DevolucaoRequest(Long vendaItemId, Integer quantidade) {
	}

	public List<ItemDevolvivel> listarItensDevolviveis(Long vendaId) {
		Venda venda = vendaService.consultarPorId(vendaId);
		List<ItemDevolvivel> lista = new ArrayList<>();
		for (VendaItem item : venda.getItens()) {
			int jaDevolvido = devolucaoItemRepository.totalDevolvidoPorVendaItem(item.getId());
			int disponivel = item.getQuantidade() - jaDevolvido;
			lista.add(new ItemDevolvivel(item.getId(), item.getProduto().getId(),
					item.getProduto().getDescricao(), item.getQuantidade(), jaDevolvido,
					disponivel, valorUnitarioEfetivo(item)));
		}
		return lista;
	}

	public List<Devolucao> listarDevolucoesVenda(Long vendaId) {
		return devolucaoRepository.findAllByVendaIdOrderByDataDevolucaoDesc(vendaId);
	}

	public List<Devolucao> consultarHistorico() {
		return devolucaoRepository.findAllByEmpresaIdOrderByDataDevolucaoDesc(
				contextoUsuarioService.getEmpresaIdObrigatoria());
	}

	@Transactional
	public Devolucao registrarDevolucao(Long vendaId, List<DevolucaoRequest> requests, String motivo)
			throws Exception {
		Venda venda = vendaService.consultarPorId(vendaId);

		if (venda.getStatus() != StatusVenda.FINALIZADA) {
			throw new Exception("Apenas vendas finalizadas podem ser devolvidas!");
		}

		Devolucao devolucao = new Devolucao();
		devolucao.setEmpresa(venda.getEmpresa());
		devolucao.setVenda(venda);
		devolucao.setDataDevolucao(LocalDateTime.now());
		devolucao.setMotivo(motivo);

		BigDecimal valorTotal = BigDecimal.ZERO;

		for (DevolucaoRequest req : requests) {
			if (req == null || req.quantidade() == null || req.quantidade() <= 0) {
				continue;
			}
			VendaItem item = venda.getItens().stream()
					.filter(i -> i.getId().equals(req.vendaItemId()))
					.findFirst()
					.orElseThrow(() -> new Exception("Item nao pertence a esta venda!"));

			int jaDevolvido = devolucaoItemRepository.totalDevolvidoPorVendaItem(item.getId());
			int disponivel = item.getQuantidade() - jaDevolvido;
			if (req.quantidade() > disponivel) {
				throw new Exception("Quantidade a devolver para '" + item.getProduto().getDescricao()
						+ "' excede o disponivel (" + disponivel + ").");
			}

			estoqueService.ajustar(item.getProduto().getId(), req.quantidade(),
					"Devolucao venda #" + venda.getId());

			BigDecimal valorUnit = valorUnitarioEfetivo(item);
			BigDecimal subtotal = valorUnit.multiply(new BigDecimal(req.quantidade()))
					.setScale(2, RoundingMode.HALF_UP);

			DevolucaoItem di = new DevolucaoItem();
			di.setVendaItem(item);
			di.setProduto(item.getProduto());
			di.setQuantidadeDevolvida(req.quantidade());
			di.setValorUnitario(valorUnit);
			di.setSubtotal(subtotal);
			devolucao.adicionarItem(di);

			valorTotal = valorTotal.add(subtotal);
		}

		if (devolucao.getItens().isEmpty()) {
			throw new Exception("Selecione ao menos um item para devolver!");
		}

		devolucao.setValorDevolvido(valorTotal);
		Devolucao salva = devolucaoRepository.save(devolucao);

		if (venda.isParcelado()) {
			ajustarParcelas(venda, valorTotal);
			vendaRepository.save(venda);
		}

		return salva;
	}

	/**
	 * Reduz proporcionalmente o valor das parcelas em aberto (PENDENTE/VENCIDO)
	 * de acordo com o valor devolvido. Parcelas zeradas sao canceladas.
	 * O abatimento e limitado ao saldo em aberto — valores ja recebidos a vista
	 * nao sao estornados automaticamente.
	 */
	private void ajustarParcelas(Venda venda, BigDecimal valorDevolvido) {
		List<Parcela> abertas = venda.getParcelas().stream()
				.filter(p -> p.getStatus() == StatusParcela.PENDENTE
						|| p.getStatus() == StatusParcela.VENCIDO)
				.sorted((a, b) -> Integer.compare(a.getNumeroParcela(), b.getNumeroParcela()))
				.toList();

		if (abertas.isEmpty()) {
			return;
		}

		BigDecimal saldo = abertas.stream()
				.map(Parcela::getValorNominal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		if (saldo.signum() <= 0) {
			return;
		}

		BigDecimal abater = valorDevolvido.min(saldo);
		BigDecimal acumulado = BigDecimal.ZERO;

		for (int i = 0; i < abertas.size(); i++) {
			Parcela p = abertas.get(i);
			boolean ultima = i == abertas.size() - 1;

			BigDecimal reducao = ultima
					? abater.subtract(acumulado)
					: abater.multiply(p.getValorNominal()).divide(saldo, 2, RoundingMode.HALF_UP);
			acumulado = acumulado.add(reducao);

			BigDecimal antes = p.getValorNominal();
			BigDecimal novo = antes.subtract(reducao);

			if (novo.signum() <= 0) {
				novo = BigDecimal.ZERO;
				p.setStatus(StatusParcela.CANCELADO);
				p.setJurosAplicados(BigDecimal.ZERO);
				p.setMultaAplicada(BigDecimal.ZERO);
			}
			p.setValorNominal(novo);
			p.setObservacao(("Ajustada por devolucao da venda #" + venda.getId()
					+ (p.getObservacao() != null ? " | " + p.getObservacao() : "")));

			logFinanceiroService.registrar("DEVOLUCAO", p, antes, novo,
					"Parcela " + p.getNumeroParcela() + "/" + p.getTotalParcelas()
							+ " ajustada por devolucao da venda #" + venda.getId());
		}
	}

	private BigDecimal valorUnitarioEfetivo(VendaItem item) {
		if (item.getQuantidade() == null || item.getQuantidade() == 0) {
			return BigDecimal.ZERO;
		}
		return item.getSubtotal().divide(new BigDecimal(item.getQuantidade()), 2, RoundingMode.HALF_UP);
	}

}
