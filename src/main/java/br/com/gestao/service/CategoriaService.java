package br.com.gestao.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import br.com.gestao.entity.Categoria;
import br.com.gestao.repository.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;

	public CategoriaService(CategoriaRepository categoriaRepository) {
		this.categoriaRepository = categoriaRepository;
	}

	public List<Categoria> consultar() {
		return categoriaRepository.findAll(Sort.by("nome"));
	}

	public Categoria consultarPorId(Long id) {
		return categoriaRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
	}

	private Categoria consultarId(Long id) {
		return categoriaRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
	}

	@Transactional
	public void salvar(Categoria categoria) throws Exception {
		if (categoria.getNome() == null || categoria.getNome().isBlank()) {
			throw new Exception("Nome inválido!");
		}
		if (categoriaRepository.existsByNome(categoria.getNome())) {
			throw new Exception("Nome já existente!");
		}
		categoriaRepository.save(categoria);
	}

	@Transactional
	public void excluir(Categoria categoria) throws Exception {
		if (categoria.getId() == null) {
			throw new Exception("ID não encontrado!");
		}
		categoriaRepository.delete(consultarId(categoria.getId()));
	}

}
