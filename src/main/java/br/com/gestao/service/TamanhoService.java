package br.com.gestao.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.gestao.entity.Tamanho;
import br.com.gestao.repository.TamanhoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class TamanhoService {

	private final TamanhoRepository tamanhoRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public TamanhoService(TamanhoRepository tamanhoRepository, ContextoUsuarioService contextoUsuarioService) {
		this.tamanhoRepository = tamanhoRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public List<Tamanho> consultar() {
		return tamanhoRepository.findAllByEmpresaIdOrderByDescricaoAsc(contextoUsuarioService.getEmpresaIdObrigatoria());
	}

	public Tamanho consultarPorId(Long id) {
		return tamanhoRepository.findByIdAndEmpresaId(id, contextoUsuarioService.getEmpresaIdObrigatoria())
				.orElseThrow(() -> new EntityNotFoundException("Tamanho nao encontrado!"));
	}

	@Transactional
	public void salvar(Tamanho tamanho) throws Exception {
		if (tamanho == null || tamanho.getDescricao() == null || tamanho.getDescricao().isBlank()) {
			throw new Exception("Descricao invalida!");
		}
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		String descricao = tamanho.getDescricao().trim();
		if (tamanho.getId() == null) {
			if (tamanhoRepository.existsByDescricaoIgnoreCaseAndEmpresaId(descricao, empresaId)) {
				throw new Exception("Descricao ja existente!");
			}
			tamanho.setDescricao(descricao);
			tamanho.setEmpresa(contextoUsuarioService.getEmpresaObrigatoria());
			tamanhoRepository.save(tamanho);
			return;
		}

		Tamanho salvo = consultarPorId(tamanho.getId());
		if (tamanhoRepository.existsByDescricaoIgnoreCaseAndEmpresaIdAndIdNot(descricao, empresaId, tamanho.getId())) {
			throw new Exception("Descricao ja existente!");
		}
		salvo.setDescricao(descricao);
		tamanhoRepository.save(salvo);
	}

	@Transactional
	public void excluir(Tamanho tamanho) throws Exception {
		if (tamanho == null || tamanho.getId() == null) {
			throw new Exception("ID nao encontrado!");
		}
		tamanhoRepository.delete(consultarPorId(tamanho.getId()));
	}
}
