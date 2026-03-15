package br.com.gestao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Grupo;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long>{

	boolean existsByNome(String nome);

}
