package br.com.gestao.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Devolucao extends EntityAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "venda_id", nullable = false)
	private Venda venda;

	@Column(name = "data_devolucao", nullable = false)
	private LocalDateTime dataDevolucao;

	@Column(length = 500)
	private String motivo;

	@Column(name = "valor_devolvido", nullable = false, precision = 15, scale = 2)
	private BigDecimal valorDevolvido = BigDecimal.ZERO;

	@OneToMany(mappedBy = "devolucao", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DevolucaoItem> itens = new ArrayList<>();

	public Devolucao() {
	}

	public void adicionarItem(DevolucaoItem item) {
		item.setDevolucao(this);
		this.itens.add(item);
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

	public LocalDateTime getDataDevolucao() {
		return dataDevolucao;
	}

	public void setDataDevolucao(LocalDateTime dataDevolucao) {
		this.dataDevolucao = dataDevolucao;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public BigDecimal getValorDevolvido() {
		return valorDevolvido;
	}

	public void setValorDevolvido(BigDecimal valorDevolvido) {
		this.valorDevolvido = valorDevolvido;
	}

	public List<DevolucaoItem> getItens() {
		return itens;
	}

	public void setItens(List<DevolucaoItem> itens) {
		this.itens = itens;
	}

}
