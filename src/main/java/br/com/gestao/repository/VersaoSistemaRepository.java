package br.com.gestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.VersaoSistema;

@Repository
public interface VersaoSistemaRepository extends JpaRepository<VersaoSistema, Long> {

	List<VersaoSistema> findAllByOrderByIdDesc();
}
