package br.com.gestao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.gestao.entity.Produto;
import br.com.gestao.entity.ProdutoVariacao;
import br.com.gestao.repository.ProdutoRepository;
import br.com.gestao.repository.ProdutoVariacaoRepository;
import br.com.gestao.util.SkuUtil;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ProdutoService {

	private final ProdutoRepository produtoRepository;
	private final ProdutoVariacaoRepository variacaoRepository;

	public ProdutoService(ProdutoRepository produtoRepository, ProdutoVariacaoRepository variacaoRepository) {
		this.produtoRepository = produtoRepository;
		this.variacaoRepository = variacaoRepository;
	}

	public Produto consultarProdutoPorId(Long id) {
		return produtoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
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
		produtoRepository.save(produto);
	}

	public void excluirProduto(Produto produto) {
		produtoRepository.deleteById(produto.getId());
	}

	public List<Produto> consultarProduto() {
		return produtoRepository.consultarProdutoComVariacoes();
	}

	// -----------------------------
	// Variação do Produto
	// -----------------------------

	public void salvarVariacao(ProdutoVariacao variacao) throws Exception {
		if (variacao == null)
			throw new Exception("Variação do produto não informada.");

		if (variacao.getProduto() == null || variacao.getProduto().getId() == null)
			throw new Exception("Produto não informado.");

		if (variacao.getCusto() == null)
			throw new Exception("Custo não informado.");

		if (variacao.getMargem() == null)
			throw new Exception("Margem não informada.");

		if (variacao.getPreco() == null)
			throw new Exception("Preço não informado.");

		if (variacao.getCusto().compareTo(BigDecimal.ZERO) < 0)
			throw new Exception("Custo não pode ser negativo.");

		if (variacao.getMargem().compareTo(BigDecimal.ZERO) < 0)
			throw new Exception("Margem não pode ser negativa.");

		if (variacao.getPreco().compareTo(BigDecimal.ZERO) < 0)
			throw new Exception("Preço não pode ser negativo.");

		if (variacao.getCor() != null && variacao.getCor().length() > 50)
			throw new Exception("Cor deve ter no máximo 50 caracteres.");

		if (variacao.getTamanho() != null && variacao.getTamanho().length() > 20)
			throw new Exception("Tamanho deve ter no máximo 20 caracteres.");

		if (variacao.getCodigoBarra() != null && variacao.getCodigoBarra().length() > 60)
			throw new Exception("Código de barras deve ter no máximo 60 caracteres.");

		if (variacao.getSku() != null && variacao.getSku().length() > 60)
			throw new Exception("SKU deve ter no máximo 60 caracteres.");

		if (variacao.getEstoque() == null)
			throw new Exception("Estoque não informado.");

		if (variacao.getEstoque().getQuantidade() == null)
			throw new Exception("Quantidade em estoque não informada.");

		if (variacao.getEstoque().getEstoqueMinimo() == null)
			throw new Exception("Estoque mínimo não informado.");

		if (variacao.getEstoque().getQuantidade() < 0)
			throw new Exception("Quantidade em estoque não pode ser negativa.");

		if (variacao.getEstoque().getEstoqueMinimo() < 0)
			throw new Exception("Estoque mínimo não pode ser negativo.");

		// calcula automaticamente o preço pelo custo e margem
		variacao.setPreco(calcularPreco(variacao.getCusto(), variacao.getMargem()));

		// associa corretamente o estoque à variação
		variacao.getEstoque().setProdutoVariacao(variacao);

		if ((variacao.getCor() == null || variacao.getCor().isBlank()) && (variacao.getTamanho() == null || variacao.getTamanho().isBlank()))
			throw new Exception("Informe ao menos cor ou tamanho para a variação.");

		// gera SKU se não informado
		if (variacao.getSku() == null || variacao.getSku().isBlank())
			variacao.setSku(SkuUtil.gerarSku(variacao.getProduto(), variacao));

		// valida SKU duplicado
		if (variacao.getId() == null) {
			if (variacaoRepository.existsBySku(variacao.getSku()))
				throw new Exception("Já existe uma variação com o SKU informado: " + variacao.getSku());

		} else {
			if (variacaoRepository.existsBySkuAndIdNot(variacao.getSku(), variacao.getId()))
				throw new Exception("Já existe outra variação com o SKU informado: " + variacao.getSku());

		}

		// impede duplicidade lógica da variação
		if (variacao.getId() == null) {
			if (variacaoRepository.existsByProdutoIdAndCorIgnoreCaseAndTamanhoIgnoreCase(variacao.getProduto().getId(),
					tratarTexto(variacao.getCor()), tratarTexto(variacao.getTamanho()))) {
				throw new Exception("Já existe uma variação para este produto com a mesma cor e tamanho.");
			}
		} else {
			if (variacaoRepository.existsByProdutoIdAndCorIgnoreCaseAndTamanhoIgnoreCaseAndIdNot(
					variacao.getProduto().getId(), tratarTexto(variacao.getCor()), tratarTexto(variacao.getTamanho()),
					variacao.getId())) {
				throw new Exception("Já existe outra variação para este produto com a mesma cor e tamanho.");
			}
		}

		// validação opcional: preço coerente com custo + margem
		BigDecimal precoCalculado = calcularPreco(variacao.getCusto(), variacao.getMargem());
		if (variacao.getPreco().compareTo(precoCalculado) != 0)
			throw new Exception(
					"Preço inválido. Para o custo e margem informados, o preço esperado é " + precoCalculado + ".");

		variacaoRepository.save(variacao);
	}

	private String tratarTexto(String valor) {
		return valor == null ? "" : valor.trim();
	}

	private BigDecimal calcularPreco(BigDecimal custo, BigDecimal margem) {
		return custo.add(custo.multiply(margem).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)).setScale(2,
				RoundingMode.HALF_UP);
	}

	public void excluirVariacao(ProdutoVariacao variacao) {
		variacaoRepository.deleteById(variacao.getId());
	}

	public ProdutoVariacao consultarVariacaoPorId(Long id) throws Exception {
		return variacaoRepository.findById(id).orElseThrow(() -> new Exception("Variação não encontrada!"));
	}

}
