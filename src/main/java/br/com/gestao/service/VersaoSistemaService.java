package br.com.gestao.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.com.gestao.entity.Usuario;
import br.com.gestao.entity.UsuarioVisualizacaoVersao;
import br.com.gestao.entity.VersaoSistema;
import br.com.gestao.repository.UsuarioVisualizacaoVersaoRepository;
import br.com.gestao.repository.VersaoSistemaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class VersaoSistemaService {

	private final VersaoSistemaRepository versaoSistemaRepository;
	private final UsuarioVisualizacaoVersaoRepository visualizacaoRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public VersaoSistemaService(VersaoSistemaRepository versaoSistemaRepository,
			UsuarioVisualizacaoVersaoRepository visualizacaoRepository, ContextoUsuarioService contextoUsuarioService) {
		this.versaoSistemaRepository = versaoSistemaRepository;
		this.visualizacaoRepository = visualizacaoRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public List<VersaoSistema> consultarTodas() {
		return versaoSistemaRepository.findAllByOrderByIdDesc();
	}

	public VersaoSistema consultarPorId(Long id) {
		return versaoSistemaRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Versao nao encontrada."));
	}

	@Transactional
	public void salvar(VersaoSistema versao) throws Exception {
		if (versao == null || versao.getTitulo() == null || versao.getTitulo().isBlank()) {
			throw new Exception("Titulo invalido.");
		}
		if (versao.getDescricao() == null || versao.getDescricao().isBlank()) {
			throw new Exception("Descricao invalida.");
		}

		VersaoSistema entidade = versao.getId() == null ? new VersaoSistema() : consultarPorId(versao.getId());
		entidade.setTitulo(versao.getTitulo().trim());
		entidade.setDescricao(versao.getDescricao().trim());
		versaoSistemaRepository.save(entidade);
	}

	@Transactional
	public void excluir(Long id) {
		versaoSistemaRepository.delete(consultarPorId(id));
	}

	public List<VersaoSistema> consultarPendentesDoUsuarioLogado() {
		Usuario usuario = contextoUsuarioService.getUsuarioLogado();
		if (usuario == null || usuario.getId() == null) {
			return List.of();
		}

		List<VersaoSistema> versoes = consultarTodas();
		if (versoes.isEmpty()) {
			return List.of();
		}

		Set<Long> visualizadas = new HashSet<>(visualizacaoRepository
				.findByUsuarioIdAndVersaoIdIn(usuario.getId(), versoes.stream().map(VersaoSistema::getId).toList()).stream()
				.map(item -> item.getVersao().getId()).toList());

		return versoes.stream().filter(versao -> !visualizadas.contains(versao.getId())).toList();
	}

	@Transactional
	public void registrarVisualizacao(Long versaoId) {
		Usuario usuario = contextoUsuarioService.getUsuarioLogado();
		if (usuario == null || usuario.getId() == null) {
			throw new EntityNotFoundException("Usuario nao autenticado.");
		}
		if (visualizacaoRepository.existsByUsuarioIdAndVersaoId(usuario.getId(), versaoId)) {
			return;
		}

		UsuarioVisualizacaoVersao visualizacao = new UsuarioVisualizacaoVersao();
		visualizacao.setUsuario(usuario);
		visualizacao.setVersao(consultarPorId(versaoId));
		visualizacaoRepository.save(visualizacao);
	}
}
