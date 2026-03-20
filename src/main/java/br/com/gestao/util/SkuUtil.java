package br.com.gestao.util;

import br.com.gestao.entity.Produto;

public class SkuUtil {
	private SkuUtil() {
		throw new UnsupportedOperationException("Classe utilitaria nao pode ser instanciada.");
	}

	public static String gerarSku(Produto produto) {
		if (produto == null || produto.getId() == null) {
			throw new IllegalArgumentException("Produto e ID do produto sao obrigatorios para gerar SKU.");
		}

		String prefixoDescricao = normalizar(parte(produto.getDescricao(), 4));
		String prefixoTamanho = normalizar(parte(produto.getTamanho() != null ? produto.getTamanho().getDescricao() : null, 3));

		return "PROD" + produto.getId() + "-" + prefixoDescricao + "-" + prefixoTamanho;
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
