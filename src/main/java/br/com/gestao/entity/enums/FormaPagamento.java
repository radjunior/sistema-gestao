package br.com.gestao.entity.enums;

public enum FormaPagamento {

	DINHEIRO("Dinheiro"),
	CARTAO_CREDITO("Cartao de Credito"),
	CARTAO_DEBITO("Cartao de Debito"),
	PIX("PIX");

	private final String descricao;

	FormaPagamento(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
