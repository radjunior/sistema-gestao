package br.com.gestao.entity;

public enum StatusVenda {

	FINALIZADA("Finalizada"),
	CANCELADA("Cancelada");

	private final String descricao;

	StatusVenda(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
