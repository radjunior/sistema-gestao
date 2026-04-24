package br.com.gestao.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrato comum para títulos sujeitos a juros e multa por atraso
 * (ex.: {@link Parcela} de venda e {@link TituloAPagar}).
 */
public interface TituloEncargos {

	LocalDate getDataVencimento();

	BigDecimal getValorNominal();

	BigDecimal getJurosAplicados();

	void setJurosAplicados(BigDecimal jurosAplicados);

	BigDecimal getMultaAplicada();

	void setMultaAplicada(BigDecimal multaAplicada);

	boolean isMultaCobrada();

	void setMultaCobrada(boolean multaCobrada);

}
