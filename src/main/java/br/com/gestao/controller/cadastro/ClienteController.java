package br.com.gestao.controller.cadastro;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.gestao.controller.DefaultController;
import br.com.gestao.entity.Cliente;
import br.com.gestao.service.ClienteService;

@Controller
@RequestMapping("/cadastro/cliente")
public class ClienteController extends DefaultController {

	private static final String PAGINA = "cadastro/cliente";
	private static final String REDIRECT = "redirect:/cadastro/cliente";
	private final ClienteService clienteService;

	public ClienteController(ClienteService clienteService) {
		this.clienteService = clienteService;
	}

	@GetMapping
	public String consultar(Model model, @RequestParam(required = false) Long id) {
		try {
			if (id != null) {
				model.addAttribute("cliente", clienteService.consultarPorId(id));
			}
			carregarPagina(model);
		} catch (Exception e) {
			e.printStackTrace();
			showError(model, e.getMessage());
			carregarPagina(model);
		}
		return PAGINA;
	}

	@PostMapping
	public String salvar(Model model, RedirectAttributes redirectAttributes, Cliente cliente) {
		try {
			clienteService.salvar(cliente);
			showSucesso(redirectAttributes,
					(cliente.getId() == null) ? "Cliente cadastrado com sucesso!" : "Cliente atualizado com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			showError(model, e.getMessage());
			carregarPagina(model);
			model.addAttribute("cliente", cliente);
			return PAGINA;
		}
	}

	@PostMapping("/excluir")
	public String excluir(Model model, RedirectAttributes redirectAttributes, Cliente cliente) {
		try {
			clienteService.excluir(cliente);
			showSucesso(redirectAttributes, "Cliente excluido com sucesso!");
			return REDIRECT;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage() != null && e.getMessage().contains("violates foreign key constraint")) {
				showError(model, "Nao e possivel excluir esse Cliente pois ele esta relacionado com outra entidade!");
			} else {
				showError(model, e.getMessage());
			}
			carregarPagina(model);
			return PAGINA;
		}
	}

	private void carregarPagina(Model model) {
		model.addAttribute("clientes", clienteService.consultar());
	}

}
