package br.com.gestao.controller.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.gestao.entity.LogErroAplicacao;
import br.com.gestao.service.LogErroAplicacaoService;

@Controller
@RequestMapping("/admin/logs-erros")
public class LogErroAplicacaoAdminController {

	private final LogErroAplicacaoService logErroAplicacaoService;

	public LogErroAplicacaoAdminController(LogErroAplicacaoService logErroAplicacaoService) {
		this.logErroAplicacaoService = logErroAplicacaoService;
	}

	@GetMapping
	public String consultar(Model model, @RequestParam(required = false, defaultValue = "mensagem") String filtro,
			@RequestParam(required = false) String termo) {
		List<LogErroAplicacao> logs = logErroAplicacaoService.consultar(filtro, termo);
		model.addAttribute("logs", logs);
		model.addAttribute("filtroSelecionado", filtro);
		model.addAttribute("termo", termo);
		return "admin/logs-erros";
	}
}
