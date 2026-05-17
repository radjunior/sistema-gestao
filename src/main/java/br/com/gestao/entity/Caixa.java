package br.com.gestao.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.gestao.entity.enums.StatusCaixa;
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
import jakarta.persistence.OrderBy;

@Entity
public class Caixa extends EntityAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(name = "data_abertura", nullable = false)
	private LocalDateTime dataAbertura;

	@Column(name = "data_fechamento")
	private LocalDateTime dataFechamento;

	@Column(name = "valor_abertura", nullable = false, precision = 15, scale = 2)
	private BigDecimal valorAbertura = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusCaixa status = StatusCaixa.ABERTO;

	@Column(name = "valor_fechamento_informado", precision = 15, scale = 2)
	private BigDecimal valorFechamentoInformado;

	@Column(name = "valor_fechamento_calculado", precision = 15, scale = 2)
	private BigDecimal valorFechamentoCalculado;

	@Column(precision = 15, scale = 2)
	private BigDecimal diferenca;

	@Column(name = "observacao_abertura", length = 500)
	private String observacaoAbertura;

	@Column(name = "observacao_fechamento", length = 500)
	private String observacaoFechamento;

	@OneToMany(mappedBy = "caixa", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("dataHora ASC")
	private List<MovimentacaoCaixa> movimentacoes = new ArrayList<>();

	public Caixa() {
	}

	public void adicionarMovimentacao(MovimentacaoCaixa movimentacao) {
		movimentacao.setCaixa(this);
		movimentacao.setEmpresa(this.empresa);
		this.movimentacoes.add(movimentacao);
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

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public LocalDateTime getDataAbertura() {
		return dataAbertura;
	}

	public void setDataAbertura(LocalDateTime dataAbertura) {
		this.dataAbertura = dataAbertura;
	}

	public LocalDateTime getDataFechamento() {
		return dataFechamento;
	}

	public void setDataFechamento(LocalDateTime dataFechamento) {
		this.dataFechamento = dataFechamento;
	}

	public BigDecimal getValorAbertura() {
		return valorAbertura;
	}

	public void setValorAbertura(BigDecimal valorAbertura) {
		this.valorAbertura = valorAbertura;
	}

	public StatusCaixa getStatus() {
		return status;
	}

	public void setStatus(StatusCaixa status) {
		this.status = status;
	}

	public BigDecimal getValorFechamentoInformado() {
		return valorFechamentoInformado;
	}

	public void setValorFechamentoInformado(BigDecimal valorFechamentoInformado) {
		this.valorFechamentoInformado = valorFechamentoInformado;
	}

	public BigDecimal getValorFechamentoCalculado() {
		return valorFechamentoCalculado;
	}

	public void setValorFechamentoCalculado(BigDecimal valorFechamentoCalculado) {
		this.valorFechamentoCalculado = valorFechamentoCalculado;
	}

	public BigDecimal getDiferenca() {
		return diferenca;
	}

	public void setDiferenca(BigDecimal diferenca) {
		this.diferenca = diferenca;
	}

	public String getObservacaoAbertura() {
		return observacaoAbertura;
	}

	public void setObservacaoAbertura(String observacaoAbertura) {
		this.observacaoAbertura = observacaoAbertura;
	}

	public String getObservacaoFechamento() {
		return observacaoFechamento;
	}

	public void setObservacaoFechamento(String observacaoFechamento) {
		this.observacaoFechamento = observacaoFechamento;
	}

	public List<MovimentacaoCaixa> getMovimentacoes() {
		return movimentacoes;
	}

	public void setMovimentacoes(List<MovimentacaoCaixa> movimentacoes) {
		this.movimentacoes = movimentacoes;
	}

}
