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

import br.com.gestao.entity.TituloAPagar;
import br.com.gestao.entity.enums.StatusParcela;

@Repository
public interface TituloAPagarRepository extends JpaRepository<TituloAPagar, Long> {

	Optional<TituloAPagar> findByIdAndEmpresaId(Long id, Long empresaId);

	List<TituloAPagar> findAllByEmpresaIdAndFornecedorIdOrderByDataVencimentoAsc(Long empresaId, Long fornecedorId);

	List<TituloAPagar> findAllByEmpresaIdAndFornecedorIdAndStatusIn(Long empresaId, Long fornecedorId, List<StatusParcela> status);

	@Query("SELECT t FROM TituloAPagar t WHERE t.status IN :status AND t.dataVencimento < :data")
	List<TituloAPagar> findGlobalByStatusInAndDataVencimentoLessThan(@Param("status") List<StatusParcela> status, @Param("data") LocalDate data);

	@Query("SELECT COALESCE(SUM(t.valorNominal + t.jurosAplicados + t.multaAplicada), 0) "
			+ "FROM TituloAPagar t WHERE t.empresa.id = :empresaId "
			+ "AND t.status IN (br.com.gestao.entity.enums.StatusParcela.PENDENTE, br.com.gestao.entity.enums.StatusParcela.VENCIDO)")
	BigDecimal somarTotalEmAberto(@Param("empresaId") Long empresaId);

	@Query("SELECT COALESCE(SUM(t.valorNominal + t.jurosAplicados + t.multaAplicada), 0) "
			+ "FROM TituloAPagar t WHERE t.empresa.id = :empresaId "
			+ "AND t.status = br.com.gestao.entity.enums.StatusParcela.VENCIDO")
	BigDecimal somarTotalVencido(@Param("empresaId") Long empresaId);

	@Query("SELECT COALESCE(SUM(t.valorPago), 0) FROM TituloAPagar t "
			+ "WHERE t.empresa.id = :empresaId AND t.status = br.com.gestao.entity.enums.StatusParcela.PAGO "
			+ "AND t.dataPagamento BETWEEN :inicio AND :fim")
	BigDecimal somarPagamentosPeriodo(@Param("empresaId") Long empresaId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

	@Query("SELECT COUNT(DISTINCT t.fornecedor.id) FROM TituloAPagar t "
			+ "WHERE t.empresa.id = :empresaId AND t.fornecedor IS NOT NULL "
			+ "AND t.status = br.com.gestao.entity.enums.StatusParcela.VENCIDO")
	Long contarFornecedoresEmAtraso(@Param("empresaId") Long empresaId);

	@Query("SELECT t.fornecedor.id, t.fornecedor.nome, "
			+ "SUM(t.valorNominal + t.jurosAplicados + t.multaAplicada), "
			+ "SUM(CASE WHEN t.status = br.com.gestao.entity.enums.StatusParcela.VENCIDO THEN 1 ELSE 0 END), "
			+ "MIN(t.dataVencimento) "
			+ "FROM TituloAPagar t "
			+ "WHERE t.empresa.id = :empresaId AND t.fornecedor IS NOT NULL "
			+ "AND t.status IN (br.com.gestao.entity.enums.StatusParcela.PENDENTE, br.com.gestao.entity.enums.StatusParcela.VENCIDO) "
			+ "GROUP BY t.fornecedor.id, t.fornecedor.nome "
			+ "ORDER BY SUM(CASE WHEN t.status = br.com.gestao.entity.enums.StatusParcela.VENCIDO THEN 1 ELSE 0 END) DESC, MIN(t.dataVencimento) ASC")
	List<Object[]> consultarResumoFornecedoresCredores(@Param("empresaId") Long empresaId);

	@Query("SELECT t FROM TituloAPagar t WHERE t.empresa.id = :empresaId "
			+ "AND (:status IS NULL OR t.status = :status) "
			+ "AND (:inicio IS NULL OR t.dataVencimento >= :inicio) "
			+ "AND (:fim IS NULL OR t.dataVencimento <= :fim) "
			+ "AND (:fornecedorId IS NULL OR t.fornecedor.id = :fornecedorId) "
			+ "ORDER BY CASE WHEN t.status = br.com.gestao.entity.enums.StatusParcela.VENCIDO THEN 0 ELSE 1 END, t.dataVencimento ASC")
	List<TituloAPagar> filtrar(@Param("empresaId") Long empresaId,
			@Param("status") StatusParcela status,
			@Param("inicio") LocalDate inicio,
			@Param("fim") LocalDate fim,
			@Param("fornecedorId") Long fornecedorId);

	@Query("SELECT MAX(t.jurosAtualizadosEm) FROM TituloAPagar t WHERE t.empresa.id = :empresaId")
	LocalDateTime ultimaAtualizacaoJuros(@Param("empresaId") Long empresaId);

}
