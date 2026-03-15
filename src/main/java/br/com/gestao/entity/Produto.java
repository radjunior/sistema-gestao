package br.com.gestao.entity;

import java.math.BigDecimal;

import org.springframework.format.annotation.NumberFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

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

	@Column(nullable = false, unique = true, length = 60)
	private String sku;

	@Column(name = "codigo_barra", length = 60)
	private String codigoBarra;

	@Column(length = 50)
	private String cor;

	@Column(length = 20)
	private String tamanho;

	@Column(nullable = false, precision = 15, scale = 2)
	@NumberFormat
	private BigDecimal custo = BigDecimal.ZERO;

	@Column(nullable = false, precision = 15, scale = 2)
	@NumberFormat
	private BigDecimal margem = BigDecimal.ZERO;

	@Column(nullable = false, precision = 15, scale = 2)
	@NumberFormat
	private BigDecimal preco = BigDecimal.ZERO;

	@OneToOne(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
	private Estoque estoque;
	
	@Column(columnDefinition = "TEXT")
	private String ncm;

	public Produto() {
		this.marca = new Marca();
		this.categoria = new Categoria();
		this.grupo = new Grupo();
		this.subgrupo = new Subgrupo();
		this.estoque = new Estoque();
		this.estoque.setProduto(this);
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

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getCodigoBarra() {
		return codigoBarra;
	}

	public void setCodigoBarra(String codigoBarra) {
		this.codigoBarra = codigoBarra;
	}

	public String getCor() {
		return cor;
	}

	public void setCor(String cor) {
		this.cor = cor;
	}

	public String getTamanho() {
		return tamanho;
	}

	public void setTamanho(String tamanho) {
		this.tamanho = tamanho;
	}

	public BigDecimal getCusto() {
		return custo;
	}

	public void setCusto(BigDecimal custo) {
		this.custo = custo;
	}

	public BigDecimal getMargem() {
		return margem;
	}

	public void setMargem(BigDecimal margem) {
		this.margem = margem;
	}

	public BigDecimal getPreco() {
		return preco;
	}

	public void setPreco(BigDecimal preco) {
		this.preco = preco;
	}

	public Estoque getEstoque() {
		return estoque;
	}

	public void setEstoque(Estoque estoque) {
		this.estoque = estoque;
		if (estoque != null) {
			estoque.setProduto(this);
		}
	}

	@Override
	public String toString() {
		return "Produto [id=" + id + ", nome=" + nome + ", descricao=" + descricao + ", sku=" + sku + ", codigoBarra="
				+ codigoBarra + ", cor=" + cor + ", tamanho=" + tamanho + ", custo=" + custo + ", margem=" + margem
				+ ", preco=" + preco + ", ativo=" + ativo + "]";
	}

}
