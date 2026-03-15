package br.com.gestao.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import br.com.gestao.entity.Marca;
import br.com.gestao.repository.MarcaRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class MarcaService {

	private final MarcaRepository marcaRepository;

	public MarcaService(MarcaRepository marcaRepository) {
		this.marcaRepository = marcaRepository;
	}

	public List<Marca> consultar() {
		return marcaRepository.findAll(Sort.by("nome"));
	}

	public void excluir(Marca marca) {
		marcaRepository.deleteById(marca.getId());
	}

	public void salvar(Marca marca) throws Exception {
		if (marca == null || marca.getNome().isBlank()) {
			throw new Exception("Nome inválido!");
		}
		if (marca.getId() == null && marcaRepository.existsByNome(marca.getNome())) {
			throw new Exception("Nome já existente!");
		}
		marcaRepository.save(marca);
	}

	public Marca consultarPorId(Long id) {
		return marcaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Marca não encontrada!"));
	}

}
