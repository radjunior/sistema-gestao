package br.com.gestao.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.gestao.entity.enums.FormaPagamento;
import br.com.gestao.entity.enums.StatusParcela;
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

@Entity
public class Parcela extends EntityAudit implements TituloEncargos {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "venda_id", nullable = false)
	private Venda venda;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

	@Column(name = "numero_parcela", nullable = false)
	private Integer numeroParcela;

	@Column(name = "total_parcelas", nullable = false)
	private Integer totalParcelas;

	@Column(name = "valor_nominal", nullable = false, precision = 15, scale = 2)
	private BigDecimal valorNominal = BigDecimal.ZERO;

	@Column(name = "valor_pago", precision = 15, scale = 2)
	private BigDecimal valorPago;

	@Column(name = "data_vencimento", nullable = false)
	private LocalDate dataVencimento;

	@Column(name = "data_pagamento")
	private LocalDate dataPagamento;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusParcela status = StatusParcela.PENDENTE;

	@Column(name = "juros_aplicados", nullable = false, precision = 15, scale = 2)
	private BigDecimal jurosAplicados = BigDecimal.ZERO;

	@Column(name = "multa_aplicada", nullable = false, precision = 15, scale = 2)
	private BigDecimal multaAplicada = BigDecimal.ZERO;

	@Column(name = "multa_cobrada", nullable = false)
	private boolean multaCobrada = false;

	@Column(name = "juros_atualizados_em")
	private LocalDateTime jurosAtualizadosEm;

	@Enumerated(EnumType.STRING)
	@Column(name = "forma_pagamento_quitacao", length = 30)
	private FormaPagamento formaPagamentoQuitacao;

	@Column(length = 500)
	private String observacao;

	public Parcela() {
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

	public Venda getVenda() {
		return venda;
	}

	public void setVenda(Venda venda) {
		this.venda = venda;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Integer getNumeroParcela() {
		return numeroParcela;
	}

	public void setNumeroParcela(Integer numeroParcela) {
		this.numeroParcela = numeroParcela;
	}

	public Integer getTotalParcelas() {
		return totalParcelas;
	}

	public void setTotalParcelas(Integer totalParcelas) {
		this.totalParcelas = totalParcelas;
	}

	public BigDecimal getValorNominal() {
		return valorNominal;
	}

	public void setValorNominal(BigDecimal valorNominal) {
		this.valorNominal = valorNominal;
	}

	public BigDecimal getValorPago() {
		return valorPago;
	}

	public void setValorPago(BigDecimal valorPago) {
		this.valorPago = valorPago;
	}

	public LocalDate getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(LocalDate dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public LocalDate getDataPagamento() {
		return dataPagamento;
	}

	public void setDataPagamento(LocalDate dataPagamento) {
		this.dataPagamento = dataPagamento;
	}

	public StatusParcela getStatus() {
		return status;
	}

	public void setStatus(StatusParcela status) {
		this.status = status;
	}

	public BigDecimal getJurosAplicados() {
		return jurosAplicados;
	}

	public void setJurosAplicados(BigDecimal jurosAplicados) {
		this.jurosAplicados = jurosAplicados;
	}

	public BigDecimal getMultaAplicada() {
		return multaAplicada;
	}

	public void setMultaAplicada(BigDecimal multaAplicada) {
		this.multaAplicada = multaAplicada;
	}

	public boolean isMultaCobrada() {
		return multaCobrada;
	}

	public void setMultaCobrada(boolean multaCobrada) {
		this.multaCobrada = multaCobrada;
	}

	public LocalDateTime getJurosAtualizadosEm() {
		return jurosAtualizadosEm;
	}

	public void setJurosAtualizadosEm(LocalDateTime jurosAtualizadosEm) {
		this.jurosAtualizadosEm = jurosAtualizadosEm;
	}

	public FormaPagamento getFormaPagamentoQuitacao() {
		return formaPagamentoQuitacao;
	}

	public void setFormaPagamentoQuitacao(FormaPagamento formaPagamentoQuitacao) {
		this.formaPagamentoQuitacao = formaPagamentoQuitacao;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public BigDecimal getValorTotalAtualizado() {
		BigDecimal juros = jurosAplicados != null ? jurosAplicados : BigDecimal.ZERO;
		BigDecimal multa = multaAplicada != null ? multaAplicada : BigDecimal.ZERO;
		return valorNominal.add(juros).add(multa);
	}

}
