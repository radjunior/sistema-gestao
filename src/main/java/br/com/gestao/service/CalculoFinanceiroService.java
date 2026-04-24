package br.com.gestao.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.gestao.entity.ConfiguracaoFinanceira;
import br.com.gestao.entity.TituloEncargos;

/**
 * Servico puro de calculo: Price, juros e multa.
 * Nao acessa banco nem contexto; recebe tudo via parametro.
 */
@Service
public class CalculoFinanceiroService {

	private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);

	/**
	 * Parcelamento sem juros: divide o total em n parcelas (ultima ajusta diferenca de centavos).
	 */
	public List<BigDecimal> calcularParcelasSemJuros(BigDecimal total, int quantidade) {
		validar(total, quantidade);
		BigDecimal valorParcela = total.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.HALF_UP);
		List<BigDecimal> parcelas = new ArrayList<>(quantidade);
		BigDecimal acumulado = BigDecimal.ZERO;
		for (int i = 1; i < quantidade; i++) {
			parcelas.add(valorParcela);
			acumulado = acumulado.add(valorParcela);
		}
		BigDecimal ultima = total.subtract(acumulado).setScale(2, RoundingMode.HALF_UP);
		parcelas.add(ultima);
		return parcelas;
	}

	/**
	 * Formula Price: PMT = PV * [ i / (1 - (1 + i)^-n) ]
	 * Taxa mensal em percentual (ex: 2.0 = 2%).
	 * Retorna o valor fixo de cada parcela.
	 */
	public BigDecimal calcularParcelaPrice(BigDecimal total, int quantidade, BigDecimal taxaMensalPercentual) {
		validar(total, quantidade);
		if (taxaMensalPercentual == null || taxaMensalPercentual.signum() <= 0) {
			return total.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.HALF_UP);
		}
		BigDecimal i = taxaMensalPercentual.divide(BigDecimal.valueOf(100), MC);
		BigDecimal umMaisI = BigDecimal.ONE.add(i);
		double pow = Math.pow(umMaisI.doubleValue(), -quantidade);
		BigDecimal denominador = BigDecimal.ONE.subtract(new BigDecimal(pow, MC), MC);
		BigDecimal fator = i.divide(denominador, MC);
		return total.multiply(fator, MC).setScale(2, RoundingMode.HALF_UP);
	}

	public List<BigDecimal> calcularParcelasComJuros(BigDecimal total, int quantidade, BigDecimal taxaMensalPercentual) {
		BigDecimal valor = calcularParcelaPrice(total, quantidade, taxaMensalPercentual);
		List<BigDecimal> parcelas = new ArrayList<>(quantidade);
		for (int i = 0; i < quantidade; i++) {
			parcelas.add(valor);
		}
		return parcelas;
	}

	public BigDecimal somar(List<BigDecimal> valores) {
		return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * Calcula vencimentos com base em dias da primeira parcela e intervalo mensal.
	 */
	public List<LocalDate> calcularVencimentos(LocalDate dataBase, int quantidade, int diasPrimeiraParcela) {
		List<LocalDate> datas = new ArrayList<>(quantidade);
		LocalDate primeiro = dataBase.plusDays(diasPrimeiraParcela);
		for (int i = 0; i < quantidade; i++) {
			datas.add(primeiro.plusMonths(i));
		}
		return datas;
	}

	/**
	 * Calcula juros e multa de uma parcela vencida em relacao a data de referencia (hoje por padrao).
	 * Atualiza in-place jurosAplicados, multaAplicada e multaCobrada.
	 * Retorna {jurosCalculado, multaCalculada}.
	 */
	public BigDecimal[] calcularEncargos(TituloEncargos titulo, ConfiguracaoFinanceira config, LocalDate referencia) {
		if (titulo == null || titulo.getDataVencimento() == null) {
			return new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO };
		}
		int carencia = config.getCarenciaDias() != null ? config.getCarenciaDias() : 0;
		long diasAtraso = ChronoUnit.DAYS.between(titulo.getDataVencimento(), referencia);
		long diasEfetivos = diasAtraso - carencia;

		if (diasEfetivos <= 0) {
			titulo.setJurosAplicados(BigDecimal.ZERO);
			if (!titulo.isMultaCobrada()) {
				titulo.setMultaAplicada(BigDecimal.ZERO);
			}
			return new BigDecimal[] { BigDecimal.ZERO, titulo.getMultaAplicada() };
		}

		BigDecimal taxa = config.getTaxaJurosMensal().divide(BigDecimal.valueOf(100), MC);
		BigDecimal valorBase = titulo.getValorNominal();
		BigDecimal juros;

		if (config.isJurosCompostos()) {
			double expoente = diasEfetivos / 30.0;
			double fator = Math.pow(1 + taxa.doubleValue(), expoente) - 1;
			juros = valorBase.multiply(new BigDecimal(fator, MC)).setScale(2, RoundingMode.HALF_UP);
		} else {
			BigDecimal fator = taxa.multiply(BigDecimal.valueOf(diasEfetivos))
					.divide(BigDecimal.valueOf(30), MC);
			juros = valorBase.multiply(fator).setScale(2, RoundingMode.HALF_UP);
		}
		if (juros.signum() < 0) {
			juros = BigDecimal.ZERO;
		}
		titulo.setJurosAplicados(juros);

		BigDecimal multa = titulo.getMultaAplicada() != null ? titulo.getMultaAplicada() : BigDecimal.ZERO;
		if (!titulo.isMultaCobrada()) {
			BigDecimal percMulta = config.getMultaAtrasoPercentual().divide(BigDecimal.valueOf(100), MC);
			multa = valorBase.multiply(percMulta).setScale(2, RoundingMode.HALF_UP);
			titulo.setMultaAplicada(multa);
			titulo.setMultaCobrada(true);
		}

		return new BigDecimal[] { juros, multa };
	}

	private void validar(BigDecimal total, int quantidade) {
		if (total == null || total.signum() < 0) {
			throw new IllegalArgumentException("Valor total invalido.");
		}
		if (quantidade < 1) {
			throw new IllegalArgumentException("Quantidade de parcelas deve ser >= 1.");
		}
	}

}
