package br.com.gestao.controller.form;

import java.time.LocalDate;

public class EmpresaCadastroForm {

	private Long empresaId;
	private String nomeFantasia;
	private String razaoSocial;
	private String cnpj;
	private String email;
	private String telefone;
	private String slug;
	private String status;
	private String plano;
	private LocalDate dataInicio;
	private LocalDate dataVencimento;
	private boolean ativo = true;
	private String adminNomeCompleto;
	private String adminUsuario;
	private String adminSenha;
	private String adminConfirmeSenha;
	private boolean adminAtivo = true;

	public Long getEmpresaId() {
		return empresaId;
	}

	public void setEmpresaId(Long empresaId) {
		this.empresaId = empresaId;
	}

	public String getNomeFantasia() {
		return nomeFantasia;
	}

	public void setNomeFantasia(String nomeFantasia) {
		this.nomeFantasia = nomeFantasia;
	}

	public String getRazaoSocial() {
		return razaoSocial;
	}

	public void setRazaoSocial(String razaoSocial) {
		this.razaoSocial = razaoSocial;
	}

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPlano() {
		return plano;
	}

	public void setPlano(String plano) {
		this.plano = plano;
	}

	public LocalDate getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(LocalDate dataInicio) {
		this.dataInicio = dataInicio;
	}

	public LocalDate getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(LocalDate dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public String getAdminNomeCompleto() {
		return adminNomeCompleto;
	}

	public void setAdminNomeCompleto(String adminNomeCompleto) {
		this.adminNomeCompleto = adminNomeCompleto;
	}

	public String getAdminUsuario() {
		return adminUsuario;
	}

	public void setAdminUsuario(String adminUsuario) {
		this.adminUsuario = adminUsuario;
	}

	public String getAdminSenha() {
		return adminSenha;
	}

	public void setAdminSenha(String adminSenha) {
		this.adminSenha = adminSenha;
	}

	public String getAdminConfirmeSenha() {
		return adminConfirmeSenha;
	}

	public void setAdminConfirmeSenha(String adminConfirmeSenha) {
		this.adminConfirmeSenha = adminConfirmeSenha;
	}

	public boolean isAdminAtivo() {
		return adminAtivo;
	}

	public void setAdminAtivo(boolean adminAtivo) {
		this.adminAtivo = adminAtivo;
	}
}
