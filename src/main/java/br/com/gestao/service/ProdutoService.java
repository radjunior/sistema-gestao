package br.com.gestao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import br.com.gestao.entity.Estoque;
import br.com.gestao.entity.Produto;
import br.com.gestao.repository.ProdutoRepository;
import br.com.gestao.util.SkuUtil;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ProdutoService {

	private final ProdutoRepository produtoRepository;

	public ProdutoService(ProdutoRepository produtoRepository) {
		this.produtoRepository = produtoRepository;
	}

	public Produto consultarProdutoPorId(Long id) {
		return produtoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
	}
	
	public void processarSkuProdutos() {
		List<Produto> produtos = consultarProduto();
		int size = produtos.size();
		int count = 0;
		for (Produto p : produtos) {
			if (p.getSku() == null || p.getSku().isBlank()) {
				count++;
				p.setSku(SkuUtil.gerarSku(p));
				Produto save = produtoRepository.save(p);
				System.out.println("[" + count + "/" + size + "] - " + save.getSku());
			}
		}
	}

	public void salvarProduto(Produto produto) throws Exception {
		if (produto == null) {
			throw new Exception("Produto inválido");
		}
		if (produto.getNome() == null || produto.getNome().isBlank()) {
			throw new Exception("Nome inválido");
		}
		if (produto.getMarca() == null || produto.getMarca().getId() == null) {
			throw new Exception("Marca inválida");
		}
		if (produto.getCategoria() == null || produto.getCategoria().getId() == null) {
			throw new Exception("Categoria inválida");
		}
		if (produto.getGrupo() == null || produto.getGrupo().getId() == null) {
			throw new Exception("Grupo inválido");
		}
		if (produto.getSubgrupo() == null || produto.getSubgrupo().getId() == null) {
			throw new Exception("Subgrupo inválido");
		}
		if (produto.getId() == null && produtoRepository.existsByNome(produto.getNome())) {
			throw new Exception("Produto já existente");
		}

		validarDadosComerciais(produto);
		configurarEstoque(produto);
		produto.setPreco(calcularPreco(produto.getCusto(), produto.getMargem()));

		if (produto.getSku() != null && !produto.getSku().isBlank()) {
			validarSku(produto);
		}

		Produto produtoSalvo = produtoRepository.save(produto);

		if (produtoSalvo.getSku() == null || produtoSalvo.getSku().isBlank()) {
			produtoSalvo.setSku(SkuUtil.gerarSku(produtoSalvo));
			validarSku(produtoSalvo);
			produtoRepository.save(produtoSalvo);
		}
	}

	private void validarDadosComerciais(Produto produto) throws Exception {
		if (produto.getCusto() == null) {
			throw new Exception("Custo não informado.");
		}
		if (produto.getMargem() == null) {
			throw new Exception("Margem não informada.");
		}
		if (produto.getCusto().compareTo(BigDecimal.ZERO) < 0) {
			throw new Exception("Custo não pode ser negativo.");
		}
		if (produto.getMargem().compareTo(BigDecimal.ZERO) < 0) {
			throw new Exception("Margem não pode ser negativa.");
		}
		if (produto.getTamanho() != null && produto.getTamanho().length() > 20) {
			throw new Exception("Tamanho deve ter no máximo 20 caracteres.");
		}
		if (produto.getCodigoBarra() != null && produto.getCodigoBarra().length() > 60) {
			throw new Exception("Código de barras deve ter no máximo 60 caracteres.");
		}
		if (produto.getSku() != null && produto.getSku().length() > 60) {
			throw new Exception("SKU deve ter no máximo 60 caracteres.");
		}
	}

	private void configurarEstoque(Produto produto) throws Exception {
		Estoque estoque = produto.getEstoque();
		if (estoque == null) {
			throw new Exception("Estoque não informado.");
		}
		if (estoque.getQuantidade() == null) {
			throw new Exception("Quantidade em estoque não informada.");
		}
		if (estoque.getEstoqueMinimo() == null) {
			throw new Exception("Estoque mínimo não informado.");
		}
		if (estoque.getQuantidade() < 0) {
			throw new Exception("Quantidade em estoque não pode ser negativa.");
		}
		if (estoque.getEstoqueMinimo() < 0) {
			throw new Exception("Estoque mínimo não pode ser negativo.");
		}
		estoque.setProduto(produto);
	}

	private void validarSku(Produto produto) throws Exception {
		if (produto.getId() == null) {
			if (produtoRepository.existsBySku(produto.getSku())) {
				throw new Exception("Já existe um produto com o SKU informado: " + produto.getSku());
			}
			return;
		}

		if (produtoRepository.existsBySkuAndIdNot(produto.getSku(), produto.getId())) {
			throw new Exception("Já existe outro produto com o SKU informado: " + produto.getSku());
		}
	}

	private BigDecimal calcularPreco(BigDecimal custo, BigDecimal margem) {
		return custo.add(custo.multiply(margem).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)).setScale(2,
				RoundingMode.HALF_UP);
	}

	public void excluirProduto(Produto produto) {
		produtoRepository.deleteById(produto.getId());
	}

	public List<Produto> consultarProduto() {
		return produtoRepository.findAll(Sort.by("nome"));
	}

}
