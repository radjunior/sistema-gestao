package br.com.gestao.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

@Entity
public class Produto extends EntityAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String nome;

	@Column(length = 1000)
	private String descricao;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "marca_id", nullable = false)
	private Marca marca;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "categoria_id", nullable = false)
	private Categoria categoria;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "grupo_id", nullable = false)
	private Grupo grupo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subgrupo_id", nullable = false)
	private Subgrupo subgrupo;

	@Column(nullable = false)
	private boolean ativo = true;

	@OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = false)
	@OrderBy("id asc")
	private List<ProdutoVariacao> variacoes = new ArrayList<>();

	public Produto() {
		this.marca = new Marca();
		this.categoria = new Categoria();
		this.grupo = new Grupo();
		this.subgrupo = new Subgrupo();
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

	public Marca getMarca() {
		return marca;
	}

	public void setMarca(Marca marca) {
		this.marca = marca;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public Grupo getGrupo() {
		return grupo;
	}

	public void setGrupo(Grupo grupo) {
		this.grupo = grupo;
	}

	public Subgrupo getSubgrupo() {
		return subgrupo;
	}

	public void setSubgrupo(Subgrupo subgrupo) {
		this.subgrupo = subgrupo;
	}

	public List<ProdutoVariacao> getVariacoes() {
		return variacoes;
	}

	public void setVariacoes(List<ProdutoVariacao> variacoes) {
		this.variacoes = variacoes;
	}

	@Override
	public String toString() {
		return "Produto [id=" + id + ", nome=" + nome + ", descricao=" + descricao + ", marca=" + marca + ", categoria="
				+ categoria + ", grupo=" + grupo + ", subgrupo=" + subgrupo + ", ativo=" + ativo + "]";
	}

}
