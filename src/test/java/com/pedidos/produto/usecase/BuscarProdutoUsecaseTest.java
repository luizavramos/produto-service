package com.pedidos.produto.usecase;

import com.pedidos.produto.domain.Produto;
import com.pedidos.produto.exception.ProdutoNaoEncontradoException;
import com.pedidos.produto.gateway.ProdutoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuscarProdutoUsecase - Testes Unitários")
class BuscarProdutoUsecaseTest {

    @Mock
    private ProdutoGateway produtoGateway;

    @InjectMocks
    private BuscarProdutoUsecase buscarProdutoUsecase;

    private Produto produto1;
    private Produto produto2;
    private Produto produto3;

    @BeforeEach
    void setUp() {
        produto1 = Produto.builder()
                .id(1L)
                .nome("Produto 1")
                .sku("PROD-001")
                .descricao("Descrição do produto 1")
                .preco(new BigDecimal("10.00"))
                .categoria("CATEGORIA_A")
                .ativo(true)
                .build();

        produto2 = Produto.builder()
                .id(2L)
                .nome("Produto 2")
                .sku("PROD-002")
                .descricao("Descrição do produto 2")
                .preco(new BigDecimal("20.00"))
                .categoria("CATEGORIA_B")
                .ativo(true)
                .build();

        produto3 = Produto.builder()
                .id(3L)
                .nome("Produto 3")
                .sku("PROD-003")
                .descricao("Descrição do produto 3")
                .preco(new BigDecimal("30.00"))
                .categoria("CATEGORIA_A")
                .ativo(false)
                .build();
    }

    @Test
    @DisplayName("Deve buscar produto por ID com sucesso")
    void deveBuscarProdutoPorIdComSucesso() {
        // Arrange
        Long id = 1L;
        when(produtoGateway.buscarPorId(id)).thenReturn(Optional.of(produto1));

        // Act
        Produto resultado = buscarProdutoUsecase.buscarPorId(id);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);
        assertThat(resultado.getNome()).isEqualTo("Produto 1");
        assertThat(resultado.getSku()).isEqualTo("PROD-001");

        verify(produtoGateway).buscarPorId(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado por ID")
    void deveLancarExcecaoQuandoProdutoNaoEncontradoPorId() {
        // Arrange
        Long id = 999L;
        when(produtoGateway.buscarPorId(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> buscarProdutoUsecase.buscarPorId(id))
                .isInstanceOf(ProdutoNaoEncontradoException.class)
                .hasMessage("Produto não encontrado com ID: " + id);

        verify(produtoGateway).buscarPorId(id);
    }

    @Test
    @DisplayName("Deve buscar produto por SKU com sucesso")
    void deveBuscarProdutoPorSkuComSucesso() {
        // Arrange
        String sku = "PROD-001";
        when(produtoGateway.buscarPorSku(sku)).thenReturn(Optional.of(produto1));

        // Act
        Produto resultado = buscarProdutoUsecase.buscarPorSku(sku);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getSku()).isEqualTo(sku);
        assertThat(resultado.getNome()).isEqualTo("Produto 1");

        verify(produtoGateway).buscarPorSku(sku);
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado por SKU")
    void deveLancarExcecaoQuandoProdutoNaoEncontradoPorSku() {
        // Arrange
        String sku = "SKU-INEXISTENTE";
        when(produtoGateway.buscarPorSku(sku)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> buscarProdutoUsecase.buscarPorSku(sku))
                .isInstanceOf(ProdutoNaoEncontradoException.class)
                .hasMessage("Produto não encontrado com SKU: " + sku);

        verify(produtoGateway).buscarPorSku(sku);
    }

    @Test
    @DisplayName("Deve buscar todos os produtos")
    void deveBuscarTodosOsProdutos() {
        // Arrange
        List<Produto> produtos = Arrays.asList(produto1, produto2, produto3);
        when(produtoGateway.buscarTodos()).thenReturn(produtos);

        // Act
        List<Produto> resultado = buscarProdutoUsecase.buscarTodos();

        // Assert
        assertThat(resultado).hasSize(3);
        assertThat(resultado).containsExactly(produto1, produto2, produto3);

        verify(produtoGateway).buscarTodos();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há produtos")
    void deveRetornarListaVaziaQuandoNaoHaProdutos() {
        // Arrange
        when(produtoGateway.buscarTodos()).thenReturn(Collections.emptyList());

        // Act
        List<Produto> resultado = buscarProdutoUsecase.buscarTodos();

        // Assert
        assertThat(resultado).isEmpty();

        verify(produtoGateway).buscarTodos();
    }

    @Test
    @DisplayName("Deve buscar apenas produtos ativos")
    void deveBuscarApenasProdutosAtivos() {
        // Arrange
        List<Produto> produtosAtivos = Arrays.asList(produto1, produto2);
        when(produtoGateway.buscarAtivos()).thenReturn(produtosAtivos);

        // Act
        List<Produto> resultado = buscarProdutoUsecase.buscarAtivos();

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado).containsExactly(produto1, produto2);
        assertThat(resultado).allMatch(Produto::isAtivo);

        verify(produtoGateway).buscarAtivos();
    }

    @Test
    @DisplayName("Deve buscar produtos por categoria")
    void deveBuscarProdutosPorCategoria() {
        // Arrange
        String categoria = "CATEGORIA_A";
        List<Produto> produtosCategoria = Arrays.asList(produto1, produto3);
        when(produtoGateway.buscarPorCategoria(categoria)).thenReturn(produtosCategoria);

        // Act
        List<Produto> resultado = buscarProdutoUsecase.buscarPorCategoria(categoria);

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado).containsExactly(produto1, produto3);
        assertThat(resultado).allMatch(p -> p.getCategoria().equals(categoria));

        verify(produtoGateway).buscarPorCategoria(categoria);
    }

    @Test
    @DisplayName("Deve buscar produtos por faixa de preço")
    void deveBuscarProdutosPorFaixaPreco() {
        // Arrange
        BigDecimal precoMin = new BigDecimal("15.00");
        BigDecimal precoMax = new BigDecimal("25.00");
        List<Produto> produtosFaixa = Arrays.asList(produto2);
        when(produtoGateway.buscarPorFaixaPreco(precoMin, precoMax)).thenReturn(produtosFaixa);

        // Act
        List<Produto> resultado = buscarProdutoUsecase.buscarPorFaixaPreco(precoMin, precoMax);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado).containsExactly(produto2);

        verify(produtoGateway).buscarPorFaixaPreco(precoMin, precoMax);
    }

    @Test
    @DisplayName("Deve lançar exceção quando preço mínimo maior que máximo")
    void deveLancarExcecaoQuandoPrecoMinimoMaiorQueMaximo() {
        // Arrange
        BigDecimal precoMin = new BigDecimal("30.00");
        BigDecimal precoMax = new BigDecimal("10.00");

        // Act & Assert
        assertThatThrownBy(() -> buscarProdutoUsecase.buscarPorFaixaPreco(precoMin, precoMax))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Preço mínimo não pode ser maior que preço máximo");
    }

    @Test
    @DisplayName("Deve buscar por faixa de preço com valores nulos")
    void deveBuscarPorFaixaPrecoComValoresNulos() {
        // Arrange
        List<Produto> todosProdutos = Arrays.asList(produto1, produto2, produto3);
        when(produtoGateway.buscarPorFaixaPreco(null, null)).thenReturn(todosProdutos);

        // Act
        List<Produto> resultado = buscarProdutoUsecase.buscarPorFaixaPreco(null, null);

        // Assert
        assertThat(resultado).hasSize(3);
        assertThat(resultado).containsExactly(produto1, produto2, produto3);

        verify(produtoGateway).buscarPorFaixaPreco(null, null);
    }

    @Test
    @DisplayName("Deve buscar por faixa de preço apenas com preço mínimo")
    void deveBuscarPorFaixaPrecoApenasComPrecoMinimo() {
        // Arrange
        BigDecimal precoMin = new BigDecimal("20.00");
        List<Produto> produtosFaixa = Arrays.asList(produto2, produto3);
        when(produtoGateway.buscarPorFaixaPreco(precoMin, null)).thenReturn(produtosFaixa);

        // Act
        List<Produto> resultado = buscarProdutoUsecase.buscarPorFaixaPreco(precoMin, null);

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado).containsExactly(produto2, produto3);

        verify(produtoGateway).buscarPorFaixaPreco(precoMin, null);
    }

    @Test
    @DisplayName("Deve buscar por faixa de preço apenas com preço máximo")
    void deveBuscarPorFaixaPrecoApenasComPrecoMaximo() {
        // Arrange
        BigDecimal precoMax = new BigDecimal("20.00");
        List<Produto> produtosFaixa = Arrays.asList(produto1, produto2);
        when(produtoGateway.buscarPorFaixaPreco(null, precoMax)).thenReturn(produtosFaixa);

        // Act
        List<Produto> resultado = buscarProdutoUsecase.buscarPorFaixaPreco(null, precoMax);

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado).containsExactly(produto1, produto2);

        verify(produtoGateway).buscarPorFaixaPreco(null, precoMax);
    }

    @Test
    @DisplayName("Deve contar total de produtos")
    void deveContarTotalDeProdutos() {
        // Arrange
        long totalEsperado = 3L;
        when(produtoGateway.contarProdutos()).thenReturn(totalEsperado);

        // Act
        long resultado = buscarProdutoUsecase.contarProdutos();

        // Assert
        assertThat(resultado).isEqualTo(totalEsperado);

        verify(produtoGateway).contarProdutos();
    }

    @Test
    @DisplayName("Deve contar produtos ativos")
    void deveContarProdutosAtivos() {
        // Arrange
        long ativosEsperado = 2L;
        when(produtoGateway.contarProdutosAtivos()).thenReturn(ativosEsperado);

        // Act
        long resultado = buscarProdutoUsecase.contarProdutosAtivos();

        // Assert
        assertThat(resultado).isEqualTo(ativosEsperado);

        verify(produtoGateway).contarProdutosAtivos();
    }
}