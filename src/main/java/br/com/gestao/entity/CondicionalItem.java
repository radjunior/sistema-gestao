package br.com.gestao.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.gestao.entity.enums.StatusItemCondicional;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "condicional_item")
public class CondicionalItem extends EntityAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "condicional_id", nullable = false)
	private Condicional condicional;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "produto_id", nullable = false)
	private Produto produto;

	@Column(nullable = false)
	private Integer quantidade = 1;

	@Column(name = "quantidade_devolvida", nullable = false)
	private Integer quantidadeDevolvida = 0;

	@Column(name = "quantidade_convertida", nullable = false)
	private Integer quantidadeConvertida = 0;

	@Column(name = "preco_unitario", nullable = false, precision = 15, scale = 2)
	private BigDecimal precoUnitario = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private StatusItemCondicional status = StatusItemCondicional.EM_PODER_CLIENTE;

	@Column(name = "data_devolucao")
	private LocalDateTime dataDevolucao;

	@Column(name = "data_conversao")
	private LocalDateTime dataConversao;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "venda_item_gerado_id")
	private VendaItem vendaItemGerado;

	public CondicionalItem() {
	}

	public int getQuantidadePendente() {
		int dev = quantidadeDevolvida != null ? quantidadeDevolvida : 0;
		int conv = quantidadeConvertida != null ? quantidadeConvertida : 0;
		int total = quantidade != null ? quantidade : 0;
		return Math.max(0, total - dev - conv);
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Condicional getCondicional() { return condicional; }
	public void setCondicional(Condicional condicional) { this.condicional = condicional; }

	public Produto getProduto() { return produto; }
	public void setProduto(Produto produto) { this.produto = produto; }

	public Integer getQuantidade() { return quantidade; }
	public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

	public Integer getQuantidadeDevolvida() { return quantidadeDevolvida; }
	public void setQuantidadeDevolvida(Integer quantidadeDevolvida) { this.quantidadeDevolvida = quantidadeDevolvida; }

	public Integer getQuantidadeConvertida() { return quantidadeConvertida; }
	public void setQuantidadeConvertida(Integer quantidadeConvertida) { this.quantidadeConvertida = quantidadeConvertida; }

	public BigDecimal getPrecoUnitario() { return precoUnitario; }
	public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

	public StatusItemCondicional getStatus() { return status; }
	public void setStatus(StatusItemCondicional status) { this.status = status; }

	public LocalDateTime getDataDevolucao() { return dataDevolucao; }
	public void setDataDevolucao(LocalDateTime dataDevolucao) { this.dataDevolucao = dataDevolucao; }

	public LocalDateTime getDataConversao() { return dataConversao; }
	public void setDataConversao(LocalDateTime dataConversao) { this.dataConversao = dataConversao; }

	public VendaItem getVendaItemGerado() { return vendaItemGerado; }
	public void setVendaItemGerado(VendaItem vendaItemGerado) { this.vendaItemGerado = vendaItemGerado; }

	@Override
	public String toString() {
		return "CondicionalItem [id=" + id + ", produtoId=" + (produto != null ? produto.getId() : null)
				+ ", quantidade=" + quantidade + ", status=" + status + "]";
	}

}
