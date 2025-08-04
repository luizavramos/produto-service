package com.pedidos.produto.gateway;

import com.pedidos.produto.domain.Produto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProdutoGateway {
    Produto salvar(Produto produto);

    Optional<Produto> buscarPorId(Long id);

    Optional<Produto> buscarPorSku(String sku);

    List<Produto> buscarTodos();

    List<Produto> buscarPorCategoria(String categoria);

    List<Produto> buscarAtivos();

    List<Produto> buscarPorFaixaPreco(BigDecimal precoMin, BigDecimal precoMax);

    void deletar(Long id);

    boolean existePorSku(String sku);

    long contarProdutos();

    long contarProdutosAtivos();
}
