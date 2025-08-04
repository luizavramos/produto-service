package com.pedidos.produto.usecase;

import com.pedidos.produto.domain.Produto;
import com.pedidos.produto.exception.SkuJaExisteException;
import com.pedidos.produto.gateway.ProdutoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CriarProdutoUsecase - Testes Unitários")
class CriarProdutoUsecaseTest {

    @Mock
    private ProdutoGateway produtoGateway;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private CriarProdutoUsecase criarProdutoUsecase;

    private String nome;
    private String sku;
    private String descricao;
    private BigDecimal preco;
    private String categoria;

    @BeforeEach
    void setUp() {
        nome = "Produto Teste";
        sku = "PROD-001";
        descricao = "Descrição do produto teste";
        preco = new BigDecimal("99.99");
        categoria = "CATEGORIA_TESTE";
    }

    @Test
    @DisplayName("Deve criar produto com sucesso quando SKU não existe")
    void devecriarProdutoComSucesso() {
        // Arrange
        when(produtoGateway.existePorSku(sku)).thenReturn(false);
        
        Produto produtoEsperado = new Produto(nome, sku, descricao, preco, categoria);
        produtoEsperado.setId(1L);
        
        when(produtoGateway.salvar(any(Produto.class))).thenReturn(produtoEsperado);

        // Act
        Produto resultado = criarProdutoUsecase.executar(nome, sku, descricao, preco, categoria);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo(nome);
        assertThat(resultado.getSku()).isEqualTo(sku);
        assertThat(resultado.getDescricao()).isEqualTo(descricao);
        assertThat(resultado.getPreco()).isEqualTo(preco);
        assertThat(resultado.getCategoria()).isEqualTo(categoria);
        assertThat(resultado.isAtivo()).isTrue();

        verify(produtoGateway).existePorSku(sku);
        verify(produtoGateway).salvar(any(Produto.class));
        verify(kafkaTemplate).send(eq("produto-events"), any(CriarProdutoUsecase.ProdutoEventMessage.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando SKU já existe")
    void deveLancarExcecaoQuandoSkuJaExiste() {
        // Arrange
        when(produtoGateway.existePorSku(sku)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> criarProdutoUsecase.executar(nome, sku, descricao, preco, categoria))
                .isInstanceOf(SkuJaExisteException.class)
                .hasMessage("SKU já cadastrado no sistema: " + sku);

        verify(produtoGateway).existePorSku(sku);
        verify(produtoGateway, never()).salvar(any(Produto.class));
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("Deve criar produto mesmo quando publicação no Kafka falha")
    void deveCriarProdutoMesmoQuandoKafkaFalha() {
        // Arrange
        when(produtoGateway.existePorSku(sku)).thenReturn(false);
        
        Produto produtoEsperado = new Produto(nome, sku, descricao, preco, categoria);
        produtoEsperado.setId(1L);
        
        when(produtoGateway.salvar(any(Produto.class))).thenReturn(produtoEsperado);
        when(kafkaTemplate.send(anyString(), any())).thenThrow(new RuntimeException("Kafka indisponível"));

        // Act
        Produto resultado = criarProdutoUsecase.executar(nome, sku, descricao, preco, categoria);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);

        verify(produtoGateway).existePorSku(sku);
        verify(produtoGateway).salvar(any(Produto.class));
        verify(kafkaTemplate).send(eq("produto-events"), any(CriarProdutoUsecase.ProdutoEventMessage.class));
    }

    @Test
    @DisplayName("Deve criar produto com dados mínimos obrigatórios")
    void deveCriarProdutoComDadosMinimos() {
        // Arrange
        String nomeMinimo = "AB";
        String skuMinimo = "ABC";
        BigDecimal precoZero = BigDecimal.ZERO;
        
        when(produtoGateway.existePorSku(skuMinimo)).thenReturn(false);
        
        Produto produtoEsperado = new Produto(nomeMinimo, skuMinimo, null, precoZero, null);
        produtoEsperado.setId(1L);
        
        when(produtoGateway.salvar(any(Produto.class))).thenReturn(produtoEsperado);

        // Act
        Produto resultado = criarProdutoUsecase.executar(nomeMinimo, skuMinimo, null, precoZero, null);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo(nomeMinimo);
        assertThat(resultado.getSku()).isEqualTo(skuMinimo);
        assertThat(resultado.getDescricao()).isNull();
        assertThat(resultado.getPreco()).isEqualTo(precoZero);
        assertThat(resultado.getCategoria()).isNull();
        assertThat(resultado.isAtivo()).isTrue();

        verify(produtoGateway).existePorSku(skuMinimo);
        verify(produtoGateway).salvar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve normalizar dados do produto antes de salvar")
    void deveNormalizarDadosDoProduto() {
        // Arrange
        String nomeComEspacos = "  Produto com Espaços  ";
        String skuMinusculo = "PROD-002";
        String descricaoComEspacos = "  Descrição com espaços  ";
        String categoriaComEspacos = "  categoria  ";
        
        when(produtoGateway.existePorSku("PROD-002")).thenReturn(false);
        
        Produto produtoEsperado = new Produto(nomeComEspacos, skuMinusculo, descricaoComEspacos, preco, categoriaComEspacos);
        produtoEsperado.setId(1L);
        
        when(produtoGateway.salvar(any(Produto.class))).thenReturn(produtoEsperado);

        // Act
        Produto resultado = criarProdutoUsecase.executar(nomeComEspacos, skuMinusculo, descricaoComEspacos, preco, categoriaComEspacos);

        // Assert
        assertThat(resultado.getNome()).isEqualTo("Produto com Espaços");
        assertThat(resultado.getSku()).isEqualTo("PROD-002");
        assertThat(resultado.getDescricao()).isEqualTo("Descrição com espaços");
        assertThat(resultado.getCategoria()).isEqualTo("categoria");

        verify(produtoGateway).existePorSku("PROD-002");
        verify(produtoGateway).salvar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve verificar se evento Kafka é publicado corretamente")
    void devePublicarEventoKafkaCorretamente() {
        // Arrange
        when(produtoGateway.existePorSku(sku)).thenReturn(false);
        
        Produto produtoSalvo = new Produto(nome, sku, descricao, preco, categoria);
        produtoSalvo.setId(1L);
        
        when(produtoGateway.salvar(any(Produto.class))).thenReturn(produtoSalvo);

        // Act
        criarProdutoUsecase.executar(nome, sku, descricao, preco, categoria);

        // Assert
        verify(kafkaTemplate, times(1)).send(eq("produto-events"), any(CriarProdutoUsecase.ProdutoEventMessage.class));
    }
}