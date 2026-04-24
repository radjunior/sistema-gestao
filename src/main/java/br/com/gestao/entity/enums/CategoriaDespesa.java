package br.com.gestao.entity.enums;

public enum CategoriaDespesa {

	FORNECEDOR("Fornecedor"),
	ALUGUEL("Aluguel"),
	ENERGIA("Energia"),
	AGUA("Água"),
	INTERNET_TELEFONE("Internet/Telefone"),
	IMPOSTO("Imposto"),
	SALARIO("Salário"),
	PRO_LABORE("Pró-labore"),
	MANUTENCAO("Manutenção"),
	MARKETING("Marketing"),
	OUTROS("Outros");

	private final String descricao;

	CategoriaDespesa(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
