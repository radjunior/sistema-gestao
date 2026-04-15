package br.com.gestao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Estoque;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

	Optional<Estoque> findByProdutoId(Long produtoId);

}
