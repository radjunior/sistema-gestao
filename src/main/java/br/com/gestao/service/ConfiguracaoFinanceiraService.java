package br.com.gestao.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestao.entity.ConfiguracaoFinanceira;
import br.com.gestao.entity.Empresa;
import br.com.gestao.repository.ConfiguracaoFinanceiraRepository;

@Service
public class ConfiguracaoFinanceiraService {

	private final ConfiguracaoFinanceiraRepository repository;
	private final ContextoUsuarioService contextoUsuarioService;

	public ConfiguracaoFinanceiraService(ConfiguracaoFinanceiraRepository repository,
			ContextoUsuarioService contextoUsuarioService) {
		this.repository = repository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	@Transactional
	public ConfiguracaoFinanceira obterOuCriarPadrao() {
		Empresa empresa = contextoUsuarioService.getEmpresaObrigatoria();
		return repository.findByEmpresaId(empresa.getId()).orElseGet(() -> {
			ConfiguracaoFinanceira config = new ConfiguracaoFinanceira();
			config.setEmpresa(empresa);
			return repository.save(config);
		});
	}

	public ConfiguracaoFinanceira obterParaEmpresa(Long empresaId, Empresa empresa) {
		return repository.findByEmpresaId(empresaId).orElseGet(() -> {
			ConfiguracaoFinanceira config = new ConfiguracaoFinanceira();
			config.setEmpresa(empresa);
			return repository.save(config);
		});
	}

	@Transactional
	public void salvar(ConfiguracaoFinanceira form) throws Exception {
		if (form == null) {
			throw new Exception("Configuracao invalida!");
		}
		validarValores(form);

		ConfiguracaoFinanceira atual = obterOuCriarPadrao();
		atual.setTaxaJurosMensal(form.getTaxaJurosMensal());
		atual.setMultaAtrasoPercentual(form.getMultaAtrasoPercentual());
		atual.setCarenciaDias(form.getCarenciaDias());
		atual.setDiasPrimeiraParcela(form.getDiasPrimeiraParcela());
		atual.setMaxParcelas(form.getMaxParcelas());
		atual.setJurosCompostos(form.isJurosCompostos());
		atual.setTaxaJurosParcelamento(form.getTaxaJurosParcelamento());
		repository.save(atual);
	}

	private void validarValores(ConfiguracaoFinanceira c) throws Exception {
		if (nuloOuNegativo(c.getTaxaJurosMensal())) {
			throw new Exception("Taxa de juros mensal invalida!");
		}
		if (nuloOuNegativo(c.getMultaAtrasoPercentual())) {
			throw new Exception("Multa de atraso invalida!");
		}
		if (nuloOuNegativo(c.getTaxaJurosParcelamento())) {
			throw new Exception("Taxa de juros do parcelamento invalida!");
		}
		if (c.getCarenciaDias() == null || c.getCarenciaDias() < 0) {
			throw new Exception("Carencia invalida!");
		}
		if (c.getDiasPrimeiraParcela() == null || c.getDiasPrimeiraParcela() < 0) {
			throw new Exception("Dias para a primeira parcela invalido!");
		}
		if (c.getMaxParcelas() == null || c.getMaxParcelas() < 1) {
			throw new Exception("Numero maximo de parcelas invalido!");
		}
	}

	private boolean nuloOuNegativo(BigDecimal v) {
		return v == null || v.signum() < 0;
	}

}
