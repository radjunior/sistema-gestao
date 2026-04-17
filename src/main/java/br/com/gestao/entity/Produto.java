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

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;

	@Column(nullable = false, length = 150)
	private String descricao;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "marca_id", nullable = true)
	private Marca marca;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "grupo_id", nullable = false)
	private Grupo grupo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subgrupo_id", nullable = false)
	private Subgrupo subgrupo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tamanho_id")
	private Tamanho tamanho;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fornecedor_id")
	private Fornecedor fornecedor;

	@Column(nullable = false)
	private boolean ativo = true;

	@Column(nullable = false, length = 60)
	private String sku;

	@Column(name = "codigo_barra", length = 60)
	private String codigoBarra;

	@Column(name = "codigo_fabricante", length = 60)
	private String codigoFabricante;

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
		this.grupo = new Grupo();
		this.subgrupo = new Subgrupo();
		this.tamanho = new Tamanho();
		this.fornecedor = new Fornecedor();
		this.estoque = new Estoque();
		this.estoque.setProduto(this);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
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

	public Tamanho getTamanho() {
		return tamanho;
	}

	public void setTamanho(Tamanho tamanho) {
		this.tamanho = tamanho;
	}

	public Fornecedor getFornecedor() {
		return fornecedor;
	}

	public void setFornecedor(Fornecedor fornecedor) {
		this.fornecedor = fornecedor;
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

	public String getCodigoFabricante() {
		return codigoFabricante;
	}

	public void setCodigoFabricante(String codigoFabricante) {
		this.codigoFabricante = codigoFabricante;
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

	public String getNcm() {
		return ncm;
	}

	public void setNcm(String ncm) {
		this.ncm = ncm;
	}

	@Override
	public String toString() {
		return "Produto [id=" + id + ", empresa=" + (empresa != null ? empresa.getId() : null) + ", descricao="
				+ descricao + ", marca=" + marca + ", grupo=" + grupo + ", subgrupo=" + subgrupo + ", tamanho="
				+ tamanho + ", ativo=" + ativo + ", sku=" + sku + ", codigoBarra=" + codigoBarra
				+ ", codigoFabricante=" + codigoFabricante + ", custo=" + custo + ", margem=" + margem + ", preco="
				+ preco + ", estoque=" + estoque + ", ncm=" + ncm + "]";
	}
}
