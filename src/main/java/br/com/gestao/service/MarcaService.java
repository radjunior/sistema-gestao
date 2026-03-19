package br.com.gestao.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.gestao.entity.Marca;
import br.com.gestao.repository.MarcaRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class MarcaService {

	private final MarcaRepository marcaRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public MarcaService(MarcaRepository marcaRepository, ContextoUsuarioService contextoUsuarioService) {
		this.marcaRepository = marcaRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public List<Marca> consultar() {
		return marcaRepository.findAllByEmpresaIdOrderByNomeAsc(contextoUsuarioService.getEmpresaIdObrigatoria());
	}

	public void excluir(Marca marca) {
		marcaRepository.delete(consultarPorId(marca.getId()));
	}

	public void salvar(Marca marca) throws Exception {
		if (marca == null || marca.getNome() == null || marca.getNome().isBlank()) {
			throw new Exception("Nome invalido!");
		}
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		String nome = marca.getNome().trim();
		if (marca.getId() == null) {
			if (marcaRepository.existsByNomeIgnoreCaseAndEmpresaId(nome, empresaId)) {
				throw new Exception("Nome ja existente!");
			}
			marca.setNome(nome);
			marca.setEmpresa(contextoUsuarioService.getEmpresaObrigatoria());
			marcaRepository.save(marca);
			return;
		}

		Marca marcaSalva = consultarPorId(marca.getId());
		if (marcaRepository.existsByNomeIgnoreCaseAndEmpresaIdAndIdNot(nome, empresaId, marca.getId())) {
			throw new Exception("Nome ja existente!");
		}
		marcaSalva.setNome(nome);
		marcaRepository.save(marcaSalva);
	}

	public Marca consultarPorId(Long id) {
		return marcaRepository.findByIdAndEmpresaId(id, contextoUsuarioService.getEmpresaIdObrigatoria())
				.orElseThrow(() -> new EntityNotFoundException("Marca nao encontrada!"));
	}
}
