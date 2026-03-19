package br.com.gestao.controller.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.controller.form.EmpresaCadastroForm;
import br.com.gestao.entity.Empresa;
import br.com.gestao.service.EmpresaService;

@Controller
@RequestMapping("/admin/empresas")
public class EmpresaAdminController extends DefaultController {

	private static final String PAGINA = "admin/empresas";
	private static final String REDIRECT = "redirect:/admin/empresas";

	private final EmpresaService empresaService;

	public EmpresaAdminController(EmpresaService empresaService) {
		this.empresaService = empresaService;
	}

	@GetMapping
	public String consultar(Model model, @RequestParam(required = false) Long id) {
		if (id != null) {
			Empresa empresa = empresaService.consultarPorId(id);
			model.addAttribute("form", toForm(empresa));
		}
		carregarPagina(model);
		return PAGINA;
	}

	@PostMapping
	public String salvar(Model model, RedirectAttributes redirectAttributes, EmpresaCadastroForm form) {
		try {
			boolean novaEmpresa = form.getEmpresaId() == null;
			empresaService.salvarComAdministrador(form);
			showSucesso(redirectAttributes,
					novaEmpresa ? "Empresa e administrador criados com sucesso!" : "Empresa atualizada com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			model.addAttribute("form", form);
			carregarPagina(model);
			return PAGINA;
		}
	}

	private void carregarPagina(Model model) {
		if (!model.containsAttribute("form")) {
			model.addAttribute("form", new EmpresaCadastroForm());
		}
		List<Empresa> empresas = empresaService.consultarTodas();
		model.addAttribute("empresas", empresas);
		model.addAttribute("statusOptions", List.of("ATIVA", "TESTE", "SUSPENSA"));
		model.addAttribute("planoOptions", List.of("BASICO", "PRO", "PREMIUM"));
	}

	private EmpresaCadastroForm toForm(Empresa empresa) {
		EmpresaCadastroForm form = new EmpresaCadastroForm();
		form.setEmpresaId(empresa.getId());
		form.setNomeFantasia(empresa.getNomeFantasia());
		form.setRazaoSocial(empresa.getRazaoSocial());
		form.setCnpj(empresa.getCnpj());
		form.setEmail(empresa.getEmail());
		form.setTelefone(empresa.getTelefone());
		form.setSlug(empresa.getSlug());
		form.setStatus(empresa.getStatus());
		form.setPlano(empresa.getPlano());
		form.setDataInicio(empresa.getDataInicio());
		form.setDataVencimento(empresa.getDataVencimento());
		form.setAtivo(empresa.isAtivo());
		return form;
	}
}
