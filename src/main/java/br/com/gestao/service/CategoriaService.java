package br.com.gestao.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.gestao.entity.Categoria;
import br.com.gestao.repository.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public CategoriaService(CategoriaRepository categoriaRepository, ContextoUsuarioService contextoUsuarioService) {
		this.categoriaRepository = categoriaRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public List<Categoria> consultar() {
		return categoriaRepository.findAllByEmpresaIdOrderByNomeAsc(contextoUsuarioService.getEmpresaIdObrigatoria());
	}

	public Categoria consultarPorId(Long id) {
		return categoriaRepository.findByIdAndEmpresaId(id, contextoUsuarioService.getEmpresaIdObrigatoria())
				.orElseThrow(() -> new EntityNotFoundException("Categoria nao encontrada"));
	}

	private Categoria consultarId(Long id) {
		return categoriaRepository.findByIdAndEmpresaId(id, contextoUsuarioService.getEmpresaIdObrigatoria())
				.orElseThrow(() -> new EntityNotFoundException("Categoria nao encontrada"));
	}

	@Transactional
	public void salvar(Categoria categoria) throws Exception {
		if (categoria.getNome() == null || categoria.getNome().isBlank()) {
			throw new Exception("Nome invalido!");
		}
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		String nome = categoria.getNome().trim();
		if (categoria.getId() == null) {
			if (categoriaRepository.existsByNomeIgnoreCaseAndEmpresaId(nome, empresaId)) {
				throw new Exception("Nome ja existente!");
			}
			categoria.setNome(nome);
			categoria.setEmpresa(contextoUsuarioService.getEmpresaObrigatoria());
			categoriaRepository.save(categoria);
			return;
		}

		Categoria categoriaSalva = consultarId(categoria.getId());
		if (categoriaRepository.existsByNomeIgnoreCaseAndEmpresaIdAndIdNot(nome, empresaId, categoria.getId())) {
			throw new Exception("Nome ja existente!");
		}
		categoriaSalva.setNome(nome);
		categoriaRepository.save(categoriaSalva);
	}

	@Transactional
	public void excluir(Categoria categoria) throws Exception {
		if (categoria.getId() == null) {
			throw new Exception("ID nao encontrado!");
		}
		categoriaRepository.delete(consultarId(categoria.getId()));
	}
}
