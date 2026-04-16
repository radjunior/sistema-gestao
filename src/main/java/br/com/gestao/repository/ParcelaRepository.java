package br.com.gestao.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.gestao.entity.Parcela;
import br.com.gestao.entity.enums.StatusParcela;

@Repository
public interface ParcelaRepository extends JpaRepository<Parcela, Long> {

	Optional<Parcela> findByIdAndEmpresaId(Long id, Long empresaId);

	List<Parcela> findAllByEmpresaIdAndClienteIdOrderByDataVencimentoAsc(Long empresaId, Long clienteId);

	List<Parcela> findAllByEmpresaIdAndStatusInOrderByDataVencimentoAsc(Long empresaId, List<StatusParcela> status);

	List<Parcela> findAllByEmpresaIdAndStatusAndDataVencimentoLessThan(Long empresaId, StatusParcela status, LocalDate data);

	@Query("SELECT p FROM Parcela p WHERE p.status IN :status AND p.dataVencimento < :data")
	List<Parcela> findGlobalByStatusInAndDataVencimentoLessThan(@Param("status") List<StatusParcela> status, @Param("data") LocalDate data);

	@Query("SELECT COALESCE(SUM(p.valorNominal + p.jurosAplicados + p.multaAplicada), 0) "
			+ "FROM Parcela p WHERE p.empresa.id = :empresaId AND p.status IN (br.com.gestao.entity.enums.StatusParcela.PENDENTE, br.com.gestao.entity.enums.StatusParcela.VENCIDO)")
	BigDecimal somarTotalEmAberto(@Param("empresaId") Long empresaId);

	@Query("SELECT COALESCE(SUM(p.valorNominal + p.jurosAplicados + p.multaAplicada), 0) "
			+ "FROM Parcela p WHERE p.empresa.id = :empresaId AND p.status = br.com.gestao.entity.enums.StatusParcela.VENCIDO")
	BigDecimal somarTotalVencido(@Param("empresaId") Long empresaId);

	@Query("SELECT COALESCE(SUM(p.valorPago), 0) FROM Parcela p "
			+ "WHERE p.empresa.id = :empresaId AND p.status = br.com.gestao.entity.enums.StatusParcela.PAGO "
			+ "AND p.dataPagamento BETWEEN :inicio AND :fim")
	BigDecimal somarRecebimentosPeriodo(@Param("empresaId") Long empresaId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

	@Query("SELECT COUNT(DISTINCT p.cliente.id) FROM Parcela p "
			+ "WHERE p.empresa.id = :empresaId AND p.status = br.com.gestao.entity.enums.StatusParcela.VENCIDO")
	Long contarClientesInadimplentes(@Param("empresaId") Long empresaId);

	@Query("SELECT p.cliente.id, p.cliente.nome, "
			+ "SUM(p.valorNominal + p.jurosAplicados + p.multaAplicada), "
			+ "SUM(CASE WHEN p.status = br.com.gestao.entity.enums.StatusParcela.VENCIDO THEN 1 ELSE 0 END), "
			+ "MIN(p.dataVencimento) "
			+ "FROM Parcela p "
			+ "WHERE p.empresa.id = :empresaId "
			+ "AND p.status IN (br.com.gestao.entity.enums.StatusParcela.PENDENTE, br.com.gestao.entity.enums.StatusParcela.VENCIDO) "
			+ "GROUP BY p.cliente.id, p.cliente.nome "
			+ "ORDER BY SUM(CASE WHEN p.status = br.com.gestao.entity.enums.StatusParcela.VENCIDO THEN 1 ELSE 0 END) DESC, MIN(p.dataVencimento) ASC")
	List<Object[]> consultarResumoClientesDevedores(@Param("empresaId") Long empresaId);

	List<Parcela> findAllByEmpresaIdAndClienteIdAndStatusIn(Long empresaId, Long clienteId, List<StatusParcela> status);

	@Query("SELECT p FROM Parcela p WHERE p.empresa.id = :empresaId "
			+ "AND (:status IS NULL OR p.status = :status) "
			+ "AND (:inicio IS NULL OR p.dataVencimento >= :inicio) "
			+ "AND (:fim IS NULL OR p.dataVencimento <= :fim) "
			+ "AND (:clienteId IS NULL OR p.cliente.id = :clienteId) "
			+ "ORDER BY CASE WHEN p.status = br.com.gestao.entity.enums.StatusParcela.VENCIDO THEN 0 ELSE 1 END, p.dataVencimento ASC")
	List<Parcela> filtrar(@Param("empresaId") Long empresaId,
			@Param("status") StatusParcela status,
			@Param("inicio") LocalDate inicio,
			@Param("fim") LocalDate fim,
			@Param("clienteId") Long clienteId);

	@Query("SELECT MAX(p.jurosAtualizadosEm) FROM Parcela p WHERE p.empresa.id = :empresaId")
	LocalDateTime ultimaAtualizacaoJuros(@Param("empresaId") Long empresaId);

}
