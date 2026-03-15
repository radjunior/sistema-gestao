package br.com.gestao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Ncm {

	@Id
	private String codigo;
	@Column(columnDefinition = "TEXT")
	private String descricao;
	@Column(columnDefinition = "TEXT")
	private String dataInicio;
	@Column(columnDefinition = "TEXT")
	private String dataFim;
	@Column(columnDefinition = "TEXT")
	private String tipoAtoIni;
	@Column(columnDefinition = "TEXT")
	private String numeroAtoIni;
	@Column(columnDefinition = "TEXT")
	private String anoAtoIni;
	@Column(columnDefinition = "TEXT")
	private String dataUltAttNcm;
	@Column(columnDefinition = "TEXT")
	private String ato;

	public Ncm() {
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(String dataInicio) {
		this.dataInicio = dataInicio;
	}

	public String getDataFim() {
		return dataFim;
	}

	public void setDataFim(String dataFim) {
		this.dataFim = dataFim;
	}

	public String getTipoAtoIni() {
		return tipoAtoIni;
	}

	public void setTipoAtoIni(String tipoAtoIni) {
		this.tipoAtoIni = tipoAtoIni;
	}

	public String getNumeroAtoIni() {
		return numeroAtoIni;
	}

	public void setNumeroAtoIni(String numeroAtoIni) {
		this.numeroAtoIni = numeroAtoIni;
	}

	public String getAnoAtoIni() {
		return anoAtoIni;
	}

	public void setAnoAtoIni(String anoAtoIni) {
		this.anoAtoIni = anoAtoIni;
	}

	public String getDataUltAttNcm() {
		return dataUltAttNcm;
	}

	public void setDataUltAttNcm(String dataUltAttNcm) {
		this.dataUltAttNcm = dataUltAttNcm;
	}

	public String getAto() {
		return ato;
	}

	public void setAto(String ato) {
		this.ato = ato;
	}

}
