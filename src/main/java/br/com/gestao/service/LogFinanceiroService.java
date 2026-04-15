package br.com.gestao.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import br.com.gestao.entity.Empresa;
import br.com.gestao.entity.LogFinanceiro;
import br.com.gestao.entity.Parcela;
import br.com.gestao.entity.Usuario;
import br.com.gestao.repository.LogFinanceiroRepository;

@Service
public class LogFinanceiroService {

	private final LogFinanceiroRepository repository;
	private final ContextoUsuarioService contextoUsuarioService;

	public LogFinanceiroService(LogFinanceiroRepository repository, ContextoUsuarioService contextoUsuarioService) {
		this.repository = repository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public void registrar(String acao, Parcela parcela, BigDecimal valorAntes, BigDecimal valorDepois, String detalhe) {
		LogFinanceiro log = new LogFinanceiro();
		Empresa empresa = parcela != null ? parcela.getEmpresa() : contextoUsuarioService.getEmpresaLogada();
		log.setEmpresa(empresa);
		if (parcela != null) {
			log.setParcelaId(parcela.getId());
			log.setVendaId(parcela.getVenda() != null ? parcela.getVenda().getId() : null);
			log.setClienteId(parcela.getCliente() != null ? parcela.getCliente().getId() : null);
		}
		Usuario usuario = contextoUsuarioService.getUsuarioLogado();
		if (usuario != null) {
			log.setUsuarioId(usuario.getId());
			log.setUsuarioNome(usuario.getNomeCompleto());
		}
		log.setAcao(acao);
		log.setValorAntes(valorAntes);
		log.setValorDepois(valorDepois);
		log.setDetalhe(detalhe);
		repository.save(log);
	}

}
