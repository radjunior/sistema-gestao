package br.com.gestao.entity.enums;

public enum StatusParcela {

	PENDENTE("Pendente"),
	PAGO("Pago"),
	VENCIDO("Vencido"),
	NEGOCIADO("Negociado"),
	CANCELADO("Cancelado");

	private final String descricao;

	StatusParcela(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
