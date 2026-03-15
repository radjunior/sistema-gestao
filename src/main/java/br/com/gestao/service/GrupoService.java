package br.com.gestao.service;

import java.util.List;

import org.springframework.data.domain.Sort;
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

	public GrupoService(GrupoRepository grupoRepository, SubgrupoRepository subgrupoRepository) {
		this.grupoRepository = grupoRepository;
		this.subgrupoRepository = subgrupoRepository;
	}

	public List<Grupo> consultar() {
		return grupoRepository.findAll(Sort.by("nome"));
	}

	public Grupo consultarPorId(Long id) {
		return grupoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado!"));
	}

	@Transactional
	public void salvar(Grupo g) throws Exception {
		if (g == null || g.getNome().isEmpty()) {
			throw new Exception("Nome inválido!");
		}
		if (grupoRepository.existsByNome(g.getNome())) {
			throw new Exception("Grupo já existente!");
		}
		grupoRepository.save(g);
	}

	@Transactional
	public void excluir(Grupo g) throws Exception {
		if (g.getId() == null) {
			throw new Exception("ID não encontrado!");
		}
		grupoRepository.deleteById(g.getId());
	}

	@Transactional
	public void salvarSubgrupo(Subgrupo subgrupo) throws Exception {
		if (subgrupo == null || subgrupo.getNome().isBlank()) {
			throw new Exception("Nome inválido");
		}
		if (subgrupo.getGrupo() == null || subgrupo.getGrupo().getId() == null) {
			throw new Exception("Subgrupo não possui um Grupo válido!");
		}
		if (subgrupo.getId() == null && subgrupoRepository.existsByNome(subgrupo.getNome())) {
			throw new Exception("Subgrupo já existe!");
		}
		subgrupoRepository.save(subgrupo);
	}

	@Transactional
	public void excluirSubgrupo(Subgrupo sub) {
		subgrupoRepository.deleteById(sub.getId());		
	}

	public List<Subgrupo> consultarSubgrupos() {
		return subgrupoRepository.findAll(Sort.by("nome"));
	}

	public Subgrupo consultarSubgrupoPorId(Long id) {
		return subgrupoRepository.findById(id).orElseThrow(()->new EntityNotFoundException("Subgrupo não encontrado!"));
	}

}
