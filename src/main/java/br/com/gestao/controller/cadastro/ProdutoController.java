package br.com.gestao.controller.cadastro;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.gestao.entity.form.ProdutoForm;
import br.com.gestao.repository.CategoriaRepository;
import br.com.gestao.repository.MarcaRepository;
import br.com.gestao.service.ProdutoService;

@Controller
@RequestMapping("/cadastro/produto")
public class ProdutoController {

	private final static String PAGINA = "cadastro/produto";
	private final static String REDIRECT = "redirect:/produto";
	private final MarcaRepository marcaRepository;
	private final CategoriaRepository categoriaRepository;
	private final ProdutoService produtoService;

	public ProdutoController(ProdutoService produtoService, MarcaRepository marcaRepository,
			CategoriaRepository categoriaRepository) {
		this.marcaRepository = marcaRepository;
		this.categoriaRepository = categoriaRepository;
		this.produtoService = produtoService;
	}

	@GetMapping
	public String consultar(Model model) {
		model.addAttribute("produtoForm", new ProdutoForm());
		model.addAttribute("marcas", marcaRepository.findAll());
		model.addAttribute("categorias", categoriaRepository.findAll());
		return PAGINA;
	}

	@PostMapping
	public String salvar(ProdutoForm produtoForm, Model model) {
		try {
			produtoService.salvar(produtoForm);
			return REDIRECT;
		} catch (Exception e) {
			model.addAttribute("produtoForm", produtoForm);
			model.addAttribute("marcas", marcaRepository.findAll());
			model.addAttribute("categorias", categoriaRepository.findAll());
			model.addAttribute("erro", e.getMessage());
			return PAGINA;
		}
	}
}
