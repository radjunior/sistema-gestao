package br.com.gestao.entity.enums;

public enum FormaPagamento {

	DINHEIRO("Dinheiro"),
	CARTAO_CREDITO("Cartao de Credito"),
	CARTAO_DEBITO("Cartao de Debito"),
	PIX("PIX"),
	PARCELADO("Parcelado (Crediario)"),
	BOLETO("Boleto"),
	CHEQUE("Cheque"),
	OUTRO("Outro");

	private final String descricao;

	FormaPagamento(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
