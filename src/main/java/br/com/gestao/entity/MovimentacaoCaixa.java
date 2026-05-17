package br.com.gestao.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.gestao.entity.enums.FormaPagamento;
import br.com.gestao.entity.enums.TipoMovimentacaoCaixa;
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
public class MovimentacaoCaixa extends EntityAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "caixa_id", nullable = false)
	private Caixa caixa;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoMovimentacaoCaixa tipo;

	@Enumerated(EnumType.STRING)
	@Column(name = "forma_pagamento", length = 20)
	private FormaPagamento formaPagamento;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal valor = BigDecimal.ZERO;

	@Column(length = 255)
	private String descricao;

	@Column(name = "venda_id")
	private Long vendaId;

	@Column(name = "data_hora", nullable = false)
	private LocalDateTime dataHora;

	public MovimentacaoCaixa() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Caixa getCaixa() {
		return caixa;
	}

	public void setCaixa(Caixa caixa) {
		this.caixa = caixa;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public TipoMovimentacaoCaixa getTipo() {
		return tipo;
	}

	public void setTipo(TipoMovimentacaoCaixa tipo) {
		this.tipo = tipo;
	}

	public FormaPagamento getFormaPagamento() {
		return formaPagamento;
	}

	public void setFormaPagamento(FormaPagamento formaPagamento) {
		this.formaPagamento = formaPagamento;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Long getVendaId() {
		return vendaId;
	}

	public void setVendaId(Long vendaId) {
		this.vendaId = vendaId;
	}

	public LocalDateTime getDataHora() {
		return dataHora;
	}

	public void setDataHora(LocalDateTime dataHora) {
		this.dataHora = dataHora;
	}

}
