package br.com.gestao.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InadimplenciaScheduler {

	private static final Logger log = LoggerFactory.getLogger(InadimplenciaScheduler.class);

	private final ContasAReceberService contasAReceberService;
	private final ContasAPagarService contasAPagarService;
	private final CondicionalService condicionalService;

	public InadimplenciaScheduler(ContasAReceberService contasAReceberService,
			ContasAPagarService contasAPagarService,
			CondicionalService condicionalService) {
		this.contasAReceberService = contasAReceberService;
		this.contasAPagarService = contasAPagarService;
		this.condicionalService = condicionalService;
	}

	/**
	 * Executa diariamente a 01:00 (hora do servidor).
	 * Atualiza status e encargos de parcelas (a receber), titulos (a pagar) vencidos,
	 * e marca condicionais com prazo expirado como VENCIDA.
	 */
	@Scheduled(cron = "0 0 1 * * *")
	public void atualizarInadimplencia() {
		log.info("Iniciando processamento de inadimplencia...");
		try {
			int parcelas = contasAReceberService.processarInadimplenciaGlobal();
			log.info("Contas a receber: {} parcela(s) atualizada(s)", parcelas);
		} catch (Exception e) {
			log.error("Falha ao processar inadimplencia de contas a receber", e);
		}
		try {
			int titulos = contasAPagarService.processarInadimplenciaGlobal();
			log.info("Contas a pagar: {} titulo(s) atualizado(s)", titulos);
		} catch (Exception e) {
			log.error("Falha ao processar inadimplencia de contas a pagar", e);
		}
		try {
			int condicionais = condicionalService.processarVencimentosGlobal();
			log.info("Condicionais: {} marcada(s) como VENCIDA", condicionais);
		} catch (Exception e) {
			log.error("Falha ao processar vencimentos de condicionais", e);
		}
	}

}
