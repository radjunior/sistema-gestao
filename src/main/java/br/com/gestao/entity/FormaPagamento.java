package br.com.gestao.entity;

public enum FormaPagamento {

	DINHEIRO("Dinheiro"),
	PIX("PIX"),
	CARTAO_DEBITO("Cartao de Debito"),
	CARTAO_CREDITO("Cartao de Credito"),
	PARCELADO("Parcelado (Crediario)"),
	CHEQUE("Cheque"),
	BOLETO("Boleto"),
	OUTRO("Outro");

	private final String descricao;

	FormaPagamento(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
