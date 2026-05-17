package br.com.gestao.entity.enums;

public enum TipoMovimentacaoCaixa {

	ABERTURA("Abertura", true),
	VENDA("Venda", true),
	SUPRIMENTO("Suprimento", true),
	SANGRIA("Sangria", false);

	private final String descricao;
	private final boolean entrada;

	TipoMovimentacaoCaixa(String descricao, boolean entrada) {
		this.descricao = descricao;
		this.entrada = entrada;
	}

	public String getDescricao() {
		return descricao;
	}

	public boolean isEntrada() {
		return entrada;
	}

}
