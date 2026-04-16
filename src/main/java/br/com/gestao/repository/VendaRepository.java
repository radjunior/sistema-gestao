package br.com.gestao.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Venda;
import br.com.gestao.entity.enums.StatusVenda;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

	Optional<Venda> findByIdAndEmpresaId(Long id, Long empresaId);

	List<Venda> findAllByEmpresaIdOrderByDataVendaDesc(Long empresaId);

	List<Venda> findAllByEmpresaIdAndStatusOrderByDataVendaDesc(Long empresaId, StatusVenda status);

	List<Venda> findAllByEmpresaIdAndDataVendaBetweenOrderByDataVendaDesc(Long empresaId, LocalDateTime inicio, LocalDateTime fim);

}
