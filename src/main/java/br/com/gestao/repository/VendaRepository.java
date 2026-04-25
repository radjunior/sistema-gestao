package br.com.gestao.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Venda;
import br.com.gestao.entity.enums.StatusVenda;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

	Optional<Venda> findByIdAndEmpresaId(Long id, Long empresaId);

	List<Venda> findAllByEmpresaIdOrderByDataVendaDesc(Long empresaId);

	List<Venda> findAllByEmpresaIdAndStatusOrderByDataVendaDesc(Long empresaId, StatusVenda status);

	List<Venda> findAllByEmpresaIdAndDataVendaBetweenOrderByDataVendaDesc(Long empresaId, LocalDateTime inicio, LocalDateTime fim);

	@Query("SELECT COALESCE(SUM(v.valorTotal), 0) FROM Venda v WHERE v.empresa.id = :empresaId AND v.status = br.com.gestao.entity.enums.StatusVenda.FINALIZADA AND v.dataVenda BETWEEN :inicio AND :fim")
	BigDecimal somarFaturamentoPeriodo(@Param("empresaId") Long empresaId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

	@Query("SELECT COUNT(v) FROM Venda v WHERE v.empresa.id = :empresaId AND v.status <> br.com.gestao.entity.enums.StatusVenda.CANCELADA AND v.dataVenda BETWEEN :inicio AND :fim")
	Long contarVendasPeriodo(@Param("empresaId") Long empresaId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

	@Query("SELECT COALESCE(AVG(v.valorTotal), 0) FROM Venda v WHERE v.empresa.id = :empresaId AND v.status = br.com.gestao.entity.enums.StatusVenda.FINALIZADA AND v.dataVenda BETWEEN :inicio AND :fim")
	BigDecimal calcularTicketMedioPeriodo(@Param("empresaId") Long empresaId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

	@Query(value = "SELECT CAST(data_venda AS date) AS dia, COALESCE(SUM(valor_total), 0) AS total FROM venda WHERE empresa_id = :empresaId AND status = 'FINALIZADA' AND data_venda BETWEEN :inicio AND :fim GROUP BY dia ORDER BY dia", nativeQuery = true)
	List<Object[]> faturamentoPorDia(@Param("empresaId") Long empresaId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

	@Query(value = "SELECT CAST(data_venda AS date) AS dia, COALESCE(SUM(valor_total), 0) AS total FROM venda WHERE status = 'FINALIZADA' AND data_venda BETWEEN :inicio AND :fim GROUP BY dia ORDER BY dia", nativeQuery = true)
	List<Object[]> faturamentoPorDiaPlataforma(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

	@Query("SELECT v FROM Venda v LEFT JOIN FETCH v.cliente WHERE v.empresa.id = :empresaId AND v.status <> :status ORDER BY v.dataVenda DESC")
	List<Venda> findUltimasVendasComCliente(@Param("empresaId") Long empresaId, @Param("status") StatusVenda status, Pageable pageable);

}
