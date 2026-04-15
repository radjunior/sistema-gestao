package br.com.gestao.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InadimplenciaScheduler {

	private static final Logger log = LoggerFactory.getLogger(InadimplenciaScheduler.class);

	private final ContasAReceberService contasAReceberService;

	public InadimplenciaScheduler(ContasAReceberService contasAReceberService) {
		this.contasAReceberService = contasAReceberService;
	}

	/**
	 * Executa diariamente a 01:00 (hora do servidor).
	 * Atualiza status e encargos de todas as parcelas vencidas de todas as empresas.
	 */
	@Scheduled(cron = "0 0 1 * * *")
	public void atualizarInadimplencia() {
		log.info("Iniciando processamento de inadimplencia...");
		try {
			int atualizadas = contasAReceberService.processarInadimplenciaGlobal();
			log.info("Processamento de inadimplencia concluido. Parcelas atualizadas: {}", atualizadas);
		} catch (Exception e) {
			log.error("Falha ao processar inadimplencia diaria", e);
		}
	}

}
