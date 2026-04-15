package br.com.gestao.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Venda;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

	Optional<Venda> findByIdAndEmpresaId(Long id, Long empresaId);

	List<Venda> findAllByEmpresaIdOrderByDataVendaDesc(Long empresaId);

	List<Venda> findAllByEmpresaIdAndDataVendaBetweenOrderByDataVendaDesc(Long empresaId, LocalDateTime inicio, LocalDateTime fim);

}
