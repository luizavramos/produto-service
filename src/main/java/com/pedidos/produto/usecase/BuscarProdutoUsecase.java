package com.pedidos.produto.usecase;

import com.pedidos.produto.domain.Produto;
import com.pedidos.produto.exception.ProdutoNaoEncontradoException;
import com.pedidos.produto.gateway.ProdutoGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BuscarProdutoUsecase {
    private final ProdutoGateway produtoGateway;

    public Produto buscarPorId(Long id) {
        log.debug("Buscando produto por ID: {}", id);
        return produtoGateway.buscarPorId(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado com ID: " + id));
    }

    public Produto buscarPorSku(String sku) {
        log.debug("Buscando produto por SKU: {}", sku);
        return produtoGateway.buscarPorSku(sku)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado com SKU: " + sku));
    }

    public List<Produto> buscarTodos() {
        log.debug("Buscando todos os produtos");
        return produtoGateway.buscarTodos();
    }

    public List<Produto> buscarAtivos() {
        log.debug("Buscando produtos ativos");
        return produtoGateway.buscarAtivos();
    }

    public List<Produto> buscarPorCategoria(String categoria) {
        log.debug("Buscando produtos por categoria: {}", categoria);
        return produtoGateway.buscarPorCategoria(categoria);
    }

    public List<Produto> buscarPorFaixaPreco(BigDecimal precoMin, BigDecimal precoMax) {
        log.debug("Buscando produtos por faixa de preço: {} - {}", precoMin, precoMax);

        if (precoMin != null && precoMax != null && precoMin.compareTo(precoMax) > 0) {
            throw new IllegalArgumentException("Preço mínimo não pode ser maior que preço máximo");
        }

        return produtoGateway.buscarPorFaixaPreco(precoMin, precoMax);
    }

    public long contarProdutos() {
        return produtoGateway.contarProdutos();
    }

    public long contarProdutosAtivos() {
        return produtoGateway.contarProdutosAtivos();
    }
}
