package br.com.gestao.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.gestao.entity.enums.StatusCondicional;
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
import jakarta.persistence.Table;

@Entity
@Table(name = "condicional")
public class Condicional extends EntityAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

	@Column(name = "data_saida", nullable = false)
	private LocalDateTime dataSaida = LocalDateTime.now();

	@Column(name = "data_prevista_devolucao", nullable = false)
	private LocalDate dataPrevistaDevolucao;

	@Column(name = "data_fechamento")
	private LocalDateTime dataFechamento;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private StatusCondicional status = StatusCondicional.ABERTA;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "venda_gerada_id")
	private Venda vendaGerada;

	@Column(length = 500)
	private String observacao;

	@OneToMany(mappedBy = "condicional", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CondicionalItem> itens = new ArrayList<>();

	public Condicional() {
	}

	public void adicionarItem(CondicionalItem item) {
		item.setCondicional(this);
		this.itens.add(item);
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Empresa getEmpresa() { return empresa; }
	public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

	public Cliente getCliente() { return cliente; }
	public void setCliente(Cliente cliente) { this.cliente = cliente; }

	public LocalDateTime getDataSaida() { return dataSaida; }
	public void setDataSaida(LocalDateTime dataSaida) { this.dataSaida = dataSaida; }

	public LocalDate getDataPrevistaDevolucao() { return dataPrevistaDevolucao; }
	public void setDataPrevistaDevolucao(LocalDate dataPrevistaDevolucao) { this.dataPrevistaDevolucao = dataPrevistaDevolucao; }

	public LocalDateTime getDataFechamento() { return dataFechamento; }
	public void setDataFechamento(LocalDateTime dataFechamento) { this.dataFechamento = dataFechamento; }

	public StatusCondicional getStatus() { return status; }
	public void setStatus(StatusCondicional status) { this.status = status; }

	public Venda getVendaGerada() { return vendaGerada; }
	public void setVendaGerada(Venda vendaGerada) { this.vendaGerada = vendaGerada; }

	public String getObservacao() { return observacao; }
	public void setObservacao(String observacao) { this.observacao = observacao; }

	public List<CondicionalItem> getItens() { return itens; }
	public void setItens(List<CondicionalItem> itens) { this.itens = itens; }

	@Override
	public String toString() {
		return "Condicional [id=" + id + ", empresa=" + (empresa != null ? empresa.getId() : null)
				+ ", cliente=" + (cliente != null ? cliente.getId() : null)
				+ ", status=" + status + "]";
	}

}
