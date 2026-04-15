package br.com.gestao.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "venda_item")
public class VendaItem extends EntityAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "venda_id", nullable = false)
	private Venda venda;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "produto_id", nullable = false)
	private Produto produto;

	@Column(nullable = false)
	private Integer quantidade = 1;

	@Column(name = "preco_unitario", nullable = false, precision = 15, scale = 2)
	private BigDecimal precoUnitario = BigDecimal.ZERO;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal desconto = BigDecimal.ZERO;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal subtotal = BigDecimal.ZERO;

	public VendaItem() {
	}

	public void calcularSubtotal() {
		BigDecimal total = this.precoUnitario
				.multiply(new BigDecimal(this.quantidade))
				.setScale(2, RoundingMode.HALF_UP);
		BigDecimal desc = this.desconto != null ? this.desconto : BigDecimal.ZERO;
		this.subtotal = total.subtract(desc);
		if (this.subtotal.compareTo(BigDecimal.ZERO) < 0) {
			this.subtotal = BigDecimal.ZERO;
		}
	}

	// Getters e Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Venda getVenda() {
		return venda;
	}

	public void setVenda(Venda venda) {
		this.venda = venda;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public BigDecimal getPrecoUnitario() {
		return precoUnitario;
	}

	public void setPrecoUnitario(BigDecimal precoUnitario) {
		this.precoUnitario = precoUnitario;
	}

	public BigDecimal getDesconto() {
		return desconto;
	}

	public void setDesconto(BigDecimal desconto) {
		this.desconto = desconto;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	@Override
	public String toString() {
		return "VendaItem [id=" + id + ", produtoId=" + (produto != null ? produto.getId() : null)
				+ ", quantidade=" + quantidade + ", subtotal=" + subtotal + "]";
	}

}
