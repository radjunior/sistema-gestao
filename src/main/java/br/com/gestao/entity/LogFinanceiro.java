package br.com.gestao.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;

@Entity
public class LogFinanceiro {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;

	@Column(name = "parcela_id")
	private Long parcelaId;

	@Column(name = "venda_id")
	private Long vendaId;

	@Column(name = "cliente_id")
	private Long clienteId;

	@Column(name = "usuario_id")
	private Long usuarioId;

	@Column(name = "usuario_nome", length = 150)
	private String usuarioNome;

	@Column(name = "data_hora", nullable = false)
	private LocalDateTime dataHora;

	@Column(nullable = false, length = 40)
	private String acao;

	@Column(name = "valor_antes", precision = 15, scale = 2)
	private BigDecimal valorAntes;

	@Column(name = "valor_depois", precision = 15, scale = 2)
	private BigDecimal valorDepois;

	@Column(length = 1000)
	private String detalhe;

	@PrePersist
	protected void onCreate() {
		if (dataHora == null) {
			dataHora = LocalDateTime.now();
		}
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Empresa getEmpresa() { return empresa; }
	public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
	public Long getParcelaId() { return parcelaId; }
	public void setParcelaId(Long parcelaId) { this.parcelaId = parcelaId; }
	public Long getVendaId() { return vendaId; }
	public void setVendaId(Long vendaId) { this.vendaId = vendaId; }
	public Long getClienteId() { return clienteId; }
	public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
	public Long getUsuarioId() { return usuarioId; }
	public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
	public String getUsuarioNome() { return usuarioNome; }
	public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }
	public LocalDateTime getDataHora() { return dataHora; }
	public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
	public String getAcao() { return acao; }
	public void setAcao(String acao) { this.acao = acao; }
	public BigDecimal getValorAntes() { return valorAntes; }
	public void setValorAntes(BigDecimal valorAntes) { this.valorAntes = valorAntes; }
	public BigDecimal getValorDepois() { return valorDepois; }
	public void setValorDepois(BigDecimal valorDepois) { this.valorDepois = valorDepois; }
	public String getDetalhe() { return detalhe; }
	public void setDetalhe(String detalhe) { this.detalhe = detalhe; }

}
