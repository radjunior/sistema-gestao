package br.com.gestao.service;

import org.springframework.stereotype.Service;

import br.com.gestao.entity.Categoria;
import br.com.gestao.entity.Estoque;
import br.com.gestao.entity.Marca;
import br.com.gestao.entity.Produto;
import br.com.gestao.entity.ProdutoVariacao;
import br.com.gestao.entity.form.ProdutoForm;
import br.com.gestao.entity.form.ProdutoVariacaoForm;
import br.com.gestao.repository.CategoriaRepository;
import br.com.gestao.repository.MarcaRepository;
import br.com.gestao.repository.ProdutoRepository;
import jakarta.transaction.Transactional;

@Service
public class ProdutoService {

	private final ProdutoRepository produtoRepository;
	private final MarcaRepository marcaRepository;
	private final CategoriaRepository categoriaRepository;

	public ProdutoService(
			ProdutoRepository produtoRepository,
			MarcaRepository marcaRepository,
			CategoriaRepository categoriaRepository) {
		this.produtoRepository = produtoRepository;
		this.marcaRepository = marcaRepository;
		this.categoriaRepository = categoriaRepository;
	}

	@Transactional
	public Produto salvar(ProdutoForm form) {
		Produto produto = toEntity(form);
		return produtoRepository.save(produto);
	}

	private Produto toEntity(ProdutoForm form) {
		Produto produto = new Produto();

		Marca marca = marcaRepository.findById(form.getMarcaId())
				.orElseThrow(() -> new IllegalArgumentException("Marca não encontrada."));

		Categoria categoria = categoriaRepository.findById(form.getCategoriaId())
				.orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));

		produto.setNome(form.getNome());
		produto.setDescricao(form.getDescricao());
		produto.setMarca(marca);
		produto.setCategoria(categoria);
		produto.setAtivo(Boolean.TRUE.equals(form.getAtivo()));

		if (form.getVariacoes() != null) {
			for (ProdutoVariacaoForm variacaoForm : form.getVariacoes()) {

				if (variacaoVazia(variacaoForm)) {
					continue;
				}

				ProdutoVariacao variacao = new ProdutoVariacao();
				variacao.setSku(trimToNull(variacaoForm.getSku()));
				variacao.setCodigoBarra(trimToNull(variacaoForm.getCodigoBarra()));
				variacao.setCor(trimToNull(variacaoForm.getCor()));
				variacao.setTamanho(trimToNull(variacaoForm.getTamanho()));
				variacao.setCusto(
						variacaoForm.getCusto() != null ? variacaoForm.getCusto() : java.math.BigDecimal.ZERO);
				variacao.setPreco(
						variacaoForm.getPreco() != null ? variacaoForm.getPreco() : java.math.BigDecimal.ZERO);

				Estoque estoque = new Estoque();
				estoque.setQuantidade(
						variacaoForm.getQuantidade() != null ? variacaoForm.getQuantidade() : 0);
				estoque.setEstoqueMinimo(
						variacaoForm.getEstoqueMinimo() != null ? variacaoForm.getEstoqueMinimo() : 0);

				variacao.setEstoque(estoque);
				estoque.setProdutoVariacao(variacao);

				produto.addVariacao(variacao);
			}
		}

		return produto;
	}

	private boolean variacaoVazia(ProdutoVariacaoForm form) {
		return isBlank(form.getSku())
				&& isBlank(form.getCodigoBarra())
				&& isBlank(form.getCor())
				&& isBlank(form.getTamanho())
				&& form.getCusto() == null
				&& form.getPreco() == null
				&& form.getQuantidade() == null
				&& form.getEstoqueMinimo() == null;
	}

	private String trimToNull(String valor) {
		if (valor == null) {
			return null;
		}
		String texto = valor.trim();
		return texto.isEmpty() ? null : texto;
	}

	private boolean isBlank(String valor) {
		return valor == null || valor.trim().isEmpty();
	}
}
