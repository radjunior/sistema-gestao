package br.com.gestao.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class ConfiguracaoFinanceira extends EntityAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "empresa_id", nullable = false, unique = true)
	private Empresa empresa;

	@Column(name = "taxa_juros_mensal", nullable = false, precision = 10, scale = 4)
	private BigDecimal taxaJurosMensal = new BigDecimal("2.0000");

	@Column(name = "multa_atraso_percentual", nullable = false, precision = 10, scale = 4)
	private BigDecimal multaAtrasoPercentual = new BigDecimal("2.0000");

	@Column(name = "carencia_dias", nullable = false)
	private Integer carenciaDias = 0;

	@Column(name = "dias_primeira_parcela", nullable = false)
	private Integer diasPrimeiraParcela = 30;

	@Column(name = "max_parcelas", nullable = false)
	private Integer maxParcelas = 12;

	@Column(name = "juros_compostos", nullable = false)
	private boolean jurosCompostos = true;

	@Column(name = "taxa_juros_parcelamento", nullable = false, precision = 10, scale = 4)
	private BigDecimal taxaJurosParcelamento = new BigDecimal("2.5000");

	public ConfiguracaoFinanceira() {
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

	public BigDecimal getTaxaJurosMensal() {
		return taxaJurosMensal;
	}

	public void setTaxaJurosMensal(BigDecimal taxaJurosMensal) {
		this.taxaJurosMensal = taxaJurosMensal;
	}

	public BigDecimal getMultaAtrasoPercentual() {
		return multaAtrasoPercentual;
	}

	public void setMultaAtrasoPercentual(BigDecimal multaAtrasoPercentual) {
		this.multaAtrasoPercentual = multaAtrasoPercentual;
	}

	public Integer getCarenciaDias() {
		return carenciaDias;
	}

	public void setCarenciaDias(Integer carenciaDias) {
		this.carenciaDias = carenciaDias;
	}

	public Integer getDiasPrimeiraParcela() {
		return diasPrimeiraParcela;
	}

	public void setDiasPrimeiraParcela(Integer diasPrimeiraParcela) {
		this.diasPrimeiraParcela = diasPrimeiraParcela;
	}

	public Integer getMaxParcelas() {
		return maxParcelas;
	}

	public void setMaxParcelas(Integer maxParcelas) {
		this.maxParcelas = maxParcelas;
	}

	public boolean isJurosCompostos() {
		return jurosCompostos;
	}

	public void setJurosCompostos(boolean jurosCompostos) {
		this.jurosCompostos = jurosCompostos;
	}

	public BigDecimal getTaxaJurosParcelamento() {
		return taxaJurosParcelamento;
	}

	public void setTaxaJurosParcelamento(BigDecimal taxaJurosParcelamento) {
		this.taxaJurosParcelamento = taxaJurosParcelamento;
	}

}
