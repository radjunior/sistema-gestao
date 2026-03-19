package br.com.gestao.controller.form;

import java.util.ArrayList;
import java.util.List;

public class UsuarioCadastroForm {

	private Long id;
	private String nomeCompleto;
	private String usuario;
	private String senha;
	private String confirmeSenha;
	private boolean ativo = true;
	private List<Long> perfilIds = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomeCompleto() {
		return nomeCompleto;
	}

	public void setNomeCompleto(String nomeCompleto) {
		this.nomeCompleto = nomeCompleto;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getConfirmeSenha() {
		return confirmeSenha;
	}

	public void setConfirmeSenha(String confirmeSenha) {
		this.confirmeSenha = confirmeSenha;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public List<Long> getPerfilIds() {
		return perfilIds;
	}

	public void setPerfilIds(List<Long> perfilIds) {
		this.perfilIds = perfilIds;
	}
}
