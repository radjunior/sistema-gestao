package br.com.gestao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Subgrupo;

@Repository
public interface SubgrupoRepository extends JpaRepository<Subgrupo, Long>{

	boolean existsByNome(String nome);

	Optional<List<Subgrupo>> findByGrupoId(Long id);

}
