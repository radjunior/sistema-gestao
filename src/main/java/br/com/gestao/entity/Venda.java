package br.com.gestao.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.gestao.entity.enums.StatusVenda;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;

@Entity
public class Venda extends EntityAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cliente_id")
	private Cliente cliente;

	@Column(name = "data_venda", nullable = false)
	private LocalDateTime dataVenda = LocalDateTime.now();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusVenda status = StatusVenda.ABERTA;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal subtotal = BigDecimal.ZERO;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal desconto = BigDecimal.ZERO;

	@Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
	private BigDecimal valorTotal = BigDecimal.ZERO;

	@Column(length = 500)
	private String observacao;

	@OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<VendaItem> itens = new ArrayList<>();

	@OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<VendaPagamento> pagamentos = new ArrayList<>();

	public Venda() {
	}

	public void adicionarItem(VendaItem item) {
		item.setVenda(this);
		this.itens.add(item);
	}

	public void adicionarPagamento(VendaPagamento pagamento) {
		pagamento.setVenda(this);
		this.pagamentos.add(pagamento);
	}

	public void recalcularTotais() {
		this.subtotal = itens.stream()
				.map(VendaItem::getSubtotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		this.valorTotal = this.subtotal.subtract(this.desconto != null ? this.desconto : BigDecimal.ZERO);
		if (this.valorTotal.compareTo(BigDecimal.ZERO) < 0) {
			this.valorTotal = BigDecimal.ZERO;
		}
	}

	public BigDecimal getTotalPago() {
		return pagamentos.stream()
				.map(VendaPagamento::getValor)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	// Getters e Setters

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

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public LocalDateTime getDataVenda() {
		return dataVenda;
	}

	public void setDataVenda(LocalDateTime dataVenda) {
		this.dataVenda = dataVenda;
	}

	public StatusVenda getStatus() {
		return status;
	}

	public void setStatus(StatusVenda status) {
		this.status = status;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getDesconto() {
		return desconto;
	}

	public void setDesconto(BigDecimal desconto) {
		this.desconto = desconto;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public List<VendaItem> getItens() {
		return itens;
	}

	public void setItens(List<VendaItem> itens) {
		this.itens = itens;
	}

	public List<VendaPagamento> getPagamentos() {
		return pagamentos;
	}

	public void setPagamentos(List<VendaPagamento> pagamentos) {
		this.pagamentos = pagamentos;
	}

	@Override
	public String toString() {
		return "Venda [id=" + id + ", empresa=" + (empresa != null ? empresa.getId() : null)
				+ ", status=" + status + ", valorTotal=" + valorTotal + "]";
	}

}
