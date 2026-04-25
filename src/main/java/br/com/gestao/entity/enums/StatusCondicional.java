package br.com.gestao.entity.enums;

public enum StatusCondicional {

	ABERTA("Aberta"),
	PARCIALMENTE_DEVOLVIDA("Parcialmente devolvida"),
	FINALIZADA("Finalizada"),
	VENCIDA("Vencida"),
	CANCELADA("Cancelada");

	private final String descricao;

	StatusCondicional(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
