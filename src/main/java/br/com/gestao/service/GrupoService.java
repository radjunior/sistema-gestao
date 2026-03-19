package br.com.gestao.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.gestao.entity.Grupo;
import br.com.gestao.entity.Subgrupo;
import br.com.gestao.repository.GrupoRepository;
import br.com.gestao.repository.SubgrupoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class GrupoService {

	private final GrupoRepository grupoRepository;
	private final SubgrupoRepository subgrupoRepository;
	private final ContextoUsuarioService contextoUsuarioService;

	public GrupoService(GrupoRepository grupoRepository, SubgrupoRepository subgrupoRepository,
			ContextoUsuarioService contextoUsuarioService) {
		this.grupoRepository = grupoRepository;
		this.subgrupoRepository = subgrupoRepository;
		this.contextoUsuarioService = contextoUsuarioService;
	}

	public List<Grupo> consultar() {
		return grupoRepository.findAllByEmpresaIdOrderByNomeAsc(contextoUsuarioService.getEmpresaIdObrigatoria());
	}

	public Grupo consultarPorId(Long id) {
		return grupoRepository.findByIdAndEmpresaId(id, contextoUsuarioService.getEmpresaIdObrigatoria())
				.orElseThrow(() -> new EntityNotFoundException("Grupo nao encontrado!"));
	}

	@Transactional
	public void salvar(Grupo grupo) throws Exception {
		if (grupo == null || grupo.getNome() == null || grupo.getNome().isBlank()) {
			throw new Exception("Nome invalido!");
		}
		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		String nome = grupo.getNome().trim();
		if (grupo.getId() == null) {
			if (grupoRepository.existsByNomeIgnoreCaseAndEmpresaId(nome, empresaId)) {
				throw new Exception("Grupo ja existente!");
			}
			grupo.setNome(nome);
			grupo.setEmpresa(contextoUsuarioService.getEmpresaObrigatoria());
			grupoRepository.save(grupo);
			return;
		}

		Grupo grupoSalvo = consultarPorId(grupo.getId());
		if (grupoRepository.existsByNomeIgnoreCaseAndEmpresaIdAndIdNot(nome, empresaId, grupo.getId())) {
			throw new Exception("Grupo ja existente!");
		}
		grupoSalvo.setNome(nome);
		grupoRepository.save(grupoSalvo);
	}

	@Transactional
	public void excluir(Grupo grupo) throws Exception {
		if (grupo.getId() == null) {
			throw new Exception("ID nao encontrado!");
		}
		grupoRepository.delete(consultarPorId(grupo.getId()));
	}

	@Transactional
	public void salvarSubgrupo(Subgrupo subgrupo) throws Exception {
		if (subgrupo == null || subgrupo.getNome() == null || subgrupo.getNome().isBlank()) {
			throw new Exception("Nome invalido");
		}
		if (subgrupo.getGrupo() == null || subgrupo.getGrupo().getId() == null) {
			throw new Exception("Subgrupo nao possui um grupo valido!");
		}

		Long empresaId = contextoUsuarioService.getEmpresaIdObrigatoria();
		String nome = subgrupo.getNome().trim();
		Grupo grupo = consultarPorId(subgrupo.getGrupo().getId());

		if (subgrupo.getId() == null) {
			if (subgrupoRepository.existsByNomeIgnoreCaseAndEmpresaId(nome, empresaId)) {
				throw new Exception("Subgrupo ja existe!");
			}
			subgrupo.setNome(nome);
			subgrupo.setEmpresa(contextoUsuarioService.getEmpresaObrigatoria());
			subgrupo.setGrupo(grupo);
			subgrupoRepository.save(subgrupo);
			return;
		}

		Subgrupo subgrupoSalvo = consultarSubgrupoPorId(subgrupo.getId());
		if (subgrupoRepository.existsByNomeIgnoreCaseAndEmpresaIdAndIdNot(nome, empresaId, subgrupo.getId())) {
			throw new Exception("Subgrupo ja existe!");
		}
		subgrupoSalvo.setNome(nome);
		subgrupoSalvo.setGrupo(grupo);
		subgrupoRepository.save(subgrupoSalvo);
	}

	@Transactional
	public void excluirSubgrupo(Subgrupo subgrupo) {
		subgrupoRepository.delete(consultarSubgrupoPorId(subgrupo.getId()));
	}

	public List<Subgrupo> consultarSubgrupos() {
		return subgrupoRepository.findAllByEmpresaIdOrderByNomeAsc(contextoUsuarioService.getEmpresaIdObrigatoria());
	}

	public Subgrupo consultarSubgrupoPorId(Long id) {
		return subgrupoRepository.findByIdAndEmpresaId(id, contextoUsuarioService.getEmpresaIdObrigatoria())
				.orElseThrow(() -> new EntityNotFoundException("Subgrupo nao encontrado!"));
	}
}
