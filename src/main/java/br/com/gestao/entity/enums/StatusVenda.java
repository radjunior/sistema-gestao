package br.com.gestao.entity.enums;

public enum StatusVenda {

	ABERTA("Aberta"),
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
