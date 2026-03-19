package br.com.gestao.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Perfil;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {

	List<Perfil> findAllByNomeOrderByIdAsc(String nome);

	Perfil findFirstByNomeOrderByIdAsc(String nome);

	List<Perfil> findByNomeIn(Collection<String> nomes);

	List<Perfil> findAllByNomeNotOrderByNomeAsc(String nome);
}
