package com.pedidos.produto.gateway.jpa.repository;

import com.pedidos.produto.gateway.jpa.entity.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<ProdutoEntity, Long> {

    Optional<ProdutoEntity> findBySku(String sku);

    boolean existsBySku(String sku);

    List<ProdutoEntity> findByAtivoTrue();

    List<ProdutoEntity> findByCategoriaIgnoreCase(String categoria);

    List<ProdutoEntity> findByCategoriaIgnoreCaseAndAtivoTrue(String categoria);

    @Query("SELECT p FROM ProdutoEntity p WHERE p.preco BETWEEN :precoMin AND :precoMax")
    List<ProdutoEntity> findByFaixaPreco(@Param("precoMin") BigDecimal precoMin,
                                         @Param("precoMax") BigDecimal precoMax);

    @Query("SELECT p FROM ProdutoEntity p WHERE " +
            "(:precoMin IS NULL OR p.preco >= :precoMin) AND " +
            "(:precoMax IS NULL OR p.preco <= :precoMax)")
    List<ProdutoEntity> findByFaixaPrecoOpcional(@Param("precoMin") BigDecimal precoMin,
                                                 @Param("precoMax") BigDecimal precoMax);

    @Query("SELECT COUNT(p) FROM ProdutoEntity p WHERE p.ativo = true")
    long countByAtivoTrue();

    @Query("SELECT p FROM ProdutoEntity p WHERE " +
            "LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<ProdutoEntity> findByTermoBusca(@Param("termo") String termo);
}