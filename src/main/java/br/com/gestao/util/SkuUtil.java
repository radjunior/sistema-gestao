package br.com.gestao.util;

import br.com.gestao.entity.Produto;
import br.com.gestao.entity.ProdutoVariacao;

public class SkuUtil {
	private SkuUtil() {
		throw new UnsupportedOperationException("Classe utilitária não pode ser instanciada.");
	}

	public static String gerarSku(Produto produto, ProdutoVariacao variacao) {
		if (produto == null || produto.getId() == null) {
			throw new IllegalArgumentException("Produto e ID do produto são obrigatórios para gerar SKU.");
		}

		String prefixoNome = normalizar(parte(produto.getNome(), 4));
		String prefixoCor = normalizar(parte(variacao.getCor(), 3));
		String prefixoTamanho = normalizar(parte(variacao.getTamanho(), 3));

		return "PROD" + produto.getId() + "-" + prefixoNome + "-" + prefixoCor + "-" + prefixoTamanho;
	}

	private static String parte(String valor, int max) {
		if (valor == null || valor.trim().isEmpty()) {
			return "SEM";
		}
		valor = valor.trim();
		return valor.length() <= max ? valor : valor.substring(0, max);
	}

	private static String normalizar(String valor) {
		String texto = java.text.Normalizer.normalize(valor, java.text.Normalizer.Form.NFD);
		texto = texto.replaceAll("\\p{M}", "");
		texto = texto.replaceAll("[^a-zA-Z0-9]", "");
		return texto.toUpperCase();
	}
}
