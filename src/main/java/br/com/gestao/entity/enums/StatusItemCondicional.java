package br.com.gestao.entity.enums;

public enum StatusItemCondicional {

	EM_PODER_CLIENTE("Em poder do cliente"),
	PARCIALMENTE_RESOLVIDO("Parcialmente resolvido"),
	DEVOLVIDO("Devolvido"),
	CONVERTIDO_VENDA("Convertido em venda");

	private final String descricao;

	StatusItemCondicional(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
