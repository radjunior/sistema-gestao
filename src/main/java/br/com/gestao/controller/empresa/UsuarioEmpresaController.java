package br.com.gestao.controller.empresa;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.controller.form.UsuarioCadastroForm;
import br.com.gestao.entity.Usuario;
import br.com.gestao.service.UsuarioGestaoService;

@Controller
@RequestMapping("/empresa/usuarios")
public class UsuarioEmpresaController extends DefaultController {

	private static final String PAGINA = "empresa/usuarios";
	private static final String REDIRECT = "redirect:/empresa/usuarios";

	private final UsuarioGestaoService usuarioGestaoService;

	public UsuarioEmpresaController(UsuarioGestaoService usuarioGestaoService) {
		this.usuarioGestaoService = usuarioGestaoService;
	}

	@GetMapping
	public String consultar(Model model, @RequestParam(required = false) Long id) {
		if (id != null) {
			model.addAttribute("form", toForm(usuarioGestaoService.consultarPorIdDaEmpresa(id)));
		}
		carregarPagina(model);
		return PAGINA;
	}

	@PostMapping
	public String salvar(Model model, RedirectAttributes redirectAttributes, UsuarioCadastroForm form) {
		try {
			boolean novo = form.getId() == null;
			usuarioGestaoService.salvar(form);
			showSucesso(redirectAttributes, novo ? "Usuario criado com sucesso!" : "Usuario atualizado com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			model.addAttribute("form", form);
			carregarPagina(model);
			return PAGINA;
		}
	}

	@PostMapping("/excluir")
	public String excluir(Model model, RedirectAttributes redirectAttributes, @RequestParam Long id) {
		try {
			usuarioGestaoService.excluir(id);
			showSucesso(redirectAttributes, "Usuario excluido com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			carregarPagina(model);
			return PAGINA;
		}
	}

	private void carregarPagina(Model model) {
		if (!model.containsAttribute("form")) {
			model.addAttribute("form", new UsuarioCadastroForm());
		}
		model.addAttribute("usuarios", usuarioGestaoService.consultarUsuariosDaEmpresaLogada());
		model.addAttribute("perfis", usuarioGestaoService.consultarPerfisDisponiveis());
	}

	private UsuarioCadastroForm toForm(Usuario usuario) {
		UsuarioCadastroForm form = new UsuarioCadastroForm();
		form.setId(usuario.getId());
		form.setNomeCompleto(usuario.getNomeCompleto());
		form.setUsuario(usuario.getUsuario());
		form.setAtivo(usuario.isAtivo());
		form.setPerfilIds(usuario.getPerfis().stream().map(perfil -> perfil.getId()).toList());
		return form;
	}
}
