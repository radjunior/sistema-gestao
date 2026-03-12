package br.com.gestao.entity.form;

import java.util.ArrayList;
import java.util.List;

public class ProdutoForm {

	private Long id;
	private String nome;
	private String descricao;
	private Long marcaId;
	private Long categoriaId;
	private Boolean ativo = true;
	private List<ProdutoVariacaoForm> variacoes = new ArrayList<>();

	public ProdutoForm() {
		if (variacoes.isEmpty()) {
			variacoes.add(new ProdutoVariacaoForm());
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Long getMarcaId() {
		return marcaId;
	}

	public void setMarcaId(Long marcaId) {
		this.marcaId = marcaId;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(Long categoriaId) {
		this.categoriaId = categoriaId;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public List<ProdutoVariacaoForm> getVariacoes() {
		return variacoes;
	}

	public void setVariacoes(List<ProdutoVariacaoForm> variacoes) {
		this.variacoes = variacoes;
	}

	@Override
	public String toString() {
		return "ProdutoForm [id=" + id + ", nome=" + nome + ", descricao=" + descricao + ", marcaId=" + marcaId
				+ ", categoriaId=" + categoriaId + ", ativo=" + ativo + ", variacoes=" + variacoes + "]";
	}

}
