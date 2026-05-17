package br.com.gestao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Devolucao;

@Repository
public interface DevolucaoRepository extends JpaRepository<Devolucao, Long> {

	Optional<Devolucao> findByIdAndEmpresaId(Long id, Long empresaId);

	List<Devolucao> findAllByEmpresaIdOrderByDataDevolucaoDesc(Long empresaId);

	List<Devolucao> findAllByVendaIdOrderByDataDevolucaoDesc(Long vendaId);

}
