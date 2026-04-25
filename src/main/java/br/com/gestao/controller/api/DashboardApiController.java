package br.com.gestao.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gestao.dto.dashboard.DashboardDTO.DashboardPayload;
import br.com.gestao.service.ContextoUsuarioService;
import br.com.gestao.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

	private final DashboardService dashboardService;
	private final ContextoUsuarioService contextoUsuarioService;

	public DashboardApiController(DashboardService dashboardService, ContextoUsuarioService contextoUsuarioService) {
		this.dashboardService = dashboardService;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	@GetMapping
	public ResponseEntity<DashboardPayload> getDashboard() {
		if (contextoUsuarioService.isAdminSaas()) {
			return ResponseEntity.ok(dashboardService.buildSaasDashboard());
		}
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		return ResponseEntity.ok(dashboardService.buildEmpresaDashboard(empresaId));
	}
}
