package br.com.gestao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Estoque extends EntityAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "produto_variacao_id", nullable = false, unique = true)
	private ProdutoVariacao produtoVariacao;

	@Column(nullable = false)
	private Integer quantidade = 0;

	@Column(nullable = false)
	private Integer estoqueMinimo = 0;

	public Estoque() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ProdutoVariacao getProdutoVariacao() {
		return produtoVariacao;
	}

	public void setProdutoVariacao(ProdutoVariacao produtoVariacao) {
		this.produtoVariacao = produtoVariacao;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public Integer getEstoqueMinimo() {
		return estoqueMinimo;
	}

	public void setEstoqueMinimo(Integer estoqueMinimo) {
		this.estoqueMinimo = estoqueMinimo;
	}

}
