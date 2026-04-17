package br.com.gestao.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestao.entity.Estoque;
import br.com.gestao.entity.Produto;
import br.com.gestao.repository.EstoqueRepository;
import br.com.gestao.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class EstoqueService {

	public record AjusteEstoque(Long produtoId, Integer delta) {
	}

	public record AjusteResultado(Long produtoId, String descricao, Integer quantidadeAnterior,
			Integer quantidadeAtual, Integer delta) {
	}

	private final EstoqueRepository estoqueRepository;
	private final ProdutoRepository produtoRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public EstoqueService(EstoqueRepository estoqueRepository, ProdutoRepository produtoRepository,
			ContextoUsuarioService contextoUsuarioService) {
		this.estoqueRepository = estoqueRepository;
		this.produtoRepository = produtoRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public List<Produto> listarProdutos() {
		return produtoRepository
				.findAllByEmpresaIdOrderByDescricaoAsc(contextoUsuarioService.getEmpresaIdObrigatoria());
	}

	@Transactional
	public AjusteResultado ajustar(Long produtoId, Integer delta, String motivo) throws Exception {
		if (produtoId == null) {
			throw new Exception("Produto nao informado!");
		}
		if (delta == null || delta == 0) {
			throw new Exception("Informe um valor de ajuste diferente de zero!");
		}

		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		Produto produto = produtoRepository.findByIdAndEmpresaId(produtoId, empresaId)
				.orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado!"));

		Estoque estoque = estoqueRepository.findByProdutoId(produtoId).orElseGet(() -> {
			Estoque novo = new Estoque();
			novo.setProduto(produto);
			novo.setQuantidade(0);
			novo.setEstoqueMinimo(0);
			return novo;
		});

		int anterior = estoque.getQuantidade() != null ? estoque.getQuantidade() : 0;
		int atual = anterior + delta;
		if (atual < 0) {
			throw new Exception("Ajuste resultaria em estoque negativo para '" + produto.getDescricao()
					+ "'. Atual: " + anterior + ", ajuste: " + delta + ".");
		}

		estoque.setQuantidade(atual);
		estoqueRepository.save(estoque);

		// motivo: reservado para integracao futura com log de movimentacao de estoque
		return new AjusteResultado(produto.getId(), produto.getDescricao(), anterior, atual, delta);
	}

	@Transactional
	public List<AjusteResultado> ajustarEmMassa(List<AjusteEstoque> ajustes, String motivo) throws Exception {
		if (ajustes == null || ajustes.isEmpty()) {
			throw new Exception("Nenhum ajuste informado!");
		}
		List<AjusteResultado> resultados = new ArrayList<>(ajustes.size());
		for (AjusteEstoque ajuste : ajustes) {
			if (ajuste.delta() == null || ajuste.delta() == 0) {
				continue; // ignora linhas sem ajuste
			}
			resultados.add(ajustar(ajuste.produtoId(), ajuste.delta(), motivo));
		}
		if (resultados.isEmpty()) {
			throw new Exception("Nenhum produto teve ajuste efetivo!");
		}
		return resultados;
	}

}
