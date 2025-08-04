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
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

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
@DisplayName("AtualizarProdutoUsecase - Testes Unitários")
class AtualizarProdutoUsecaseTest {

    @Mock
    private ProdutoGateway produtoGateway;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private AtualizarProdutoUsecase atualizarProdutoUsecase;

    private Produto produto;
    private LocalDateTime dataAntesUpdate;

    @BeforeEach
    void setUp() {
        dataAntesUpdate = LocalDateTime.now().minusHours(1);
        
        produto = Produto.builder()
                .id(1L)
                .nome("Produto Original")
                .sku("PROD-001")
                .descricao("Descrição original")
                .preco(new BigDecimal("50.00"))
                .categoria("CATEGORIA_ORIGINAL")
                .ativo(true)
                .createdAt(dataAntesUpdate)
                .updatedAt(dataAntesUpdate)
                .build();
    }

    @Test
    @DisplayName("Deve atualizar dados do produto com sucesso")
    void deveAtualizarDadosDoProdutoComSucesso() {
        // Arrange
        Long id = 1L;
        String novoNome = "Produto Atualizado";
        String novaDescricao = "Nova descrição";
        BigDecimal novoPreco = new BigDecimal("75.00");
        String novaCategoria = "NOVA_CATEGORIA";

        when(produtoGateway.buscarPorId(id)).thenReturn(Optional.of(produto));
        when(produtoGateway.salvar(any(Produto.class))).thenReturn(produto);

        // Act
        Produto resultado = atualizarProdutoUsecase.atualizarDados(id, novoNome, novaDescricao, novoPreco, novaCategoria);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);
        assertThat(resultado.getNome()).isEqualTo(novoNome);
        assertThat(resultado.getDescricao()).isEqualTo(novaDescricao);
        assertThat(resultado.getPreco()).isEqualTo(novoPreco);
        assertThat(resultado.getCategoria()).isEqualTo(novaCategoria);

        verify(produtoGateway).buscarPorId(id);
        verify(produtoGateway).salvar(produto);
        verify(kafkaTemplate).send(eq("produto-events"), any(CriarProdutoUsecase.ProdutoEventMessage.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado para atualizar dados")
    void deveLancarExcecaoQuandoProdutoNaoEncontradoParaAtualizarDados() {
        // Arrange
        Long id = 999L;
        when(produtoGateway.buscarPorId(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> atualizarProdutoUsecase.atualizarDados(id, "Nome", "Desc", new BigDecimal("10.00"), "Cat"))
                .isInstanceOf(ProdutoNaoEncontradoException.class)
                .hasMessage("Produto não encontrado com ID: " + id);

        verify(produtoGateway).buscarPorId(id);
        verify(produtoGateway, never()).salvar(any(Produto.class));
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("Deve atualizar preço do produto com sucesso")
    void deveAtualizarPrecoDoProdutoComSucesso() {
        // Arrange
        Long id = 1L;
        BigDecimal novoPreco = new BigDecimal("99.99");

        when(produtoGateway.buscarPorId(id)).thenReturn(Optional.of(produto));
        when(produtoGateway.salvar(any(Produto.class))).thenReturn(produto);

        // Act
        Produto resultado = atualizarProdutoUsecase.atualizarPreco(id, novoPreco);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);
        assertThat(resultado.getPreco()).isEqualTo(novoPreco);

        verify(produtoGateway).buscarPorId(id);
        verify(produtoGateway).salvar(produto);
        verify(kafkaTemplate).send(eq("produto-events"), any(CriarProdutoUsecase.ProdutoEventMessage.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado para atualizar preço")
    void deveLancarExcecaoQuandoProdutoNaoEncontradoParaAtualizarPreco() {
        // Arrange
        Long id = 999L;
        BigDecimal novoPreco = new BigDecimal("99.99");
        when(produtoGateway.buscarPorId(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> atualizarProdutoUsecase.atualizarPreco(id, novoPreco))
                .isInstanceOf(ProdutoNaoEncontradoException.class)
                .hasMessage("Produto não encontrado com ID: " + id);

        verify(produtoGateway).buscarPorId(id);
        verify(produtoGateway, never()).salvar(any(Produto.class));
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("Deve ativar produto com sucesso")
    void deveAtivarProdutoComSucesso() {
        // Arrange
        Long id = 1L;
        produto.setAtivo(false); // Produto inicialmente inativo

        when(produtoGateway.buscarPorId(id)).thenReturn(Optional.of(produto));
        when(produtoGateway.salvar(any(Produto.class))).thenReturn(produto);

        // Act
        Produto resultado = atualizarProdutoUsecase.ativar(id);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);
        assertThat(resultado.isAtivo()).isTrue();

        verify(produtoGateway).buscarPorId(id);
        verify(produtoGateway).salvar(produto);
        verify(kafkaTemplate).send(eq("produto-events"), any(CriarProdutoUsecase.ProdutoEventMessage.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado para ativar")
    void deveLancarExcecaoQuandoProdutoNaoEncontradoParaAtivar() {
        // Arrange
        Long id = 999L;
        when(produtoGateway.buscarPorId(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> atualizarProdutoUsecase.ativar(id))
                .isInstanceOf(ProdutoNaoEncontradoException.class)
                .hasMessage("Produto não encontrado com ID: " + id);

        verify(produtoGateway).buscarPorId(id);
        verify(produtoGateway, never()).salvar(any(Produto.class));
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("Deve desativar produto com sucesso")
    void deveDesativarProdutoComSucesso() {
        // Arrange
        Long id = 1L;
        produto.setAtivo(true); // Produto inicialmente ativo

        when(produtoGateway.buscarPorId(id)).thenReturn(Optional.of(produto));
        when(produtoGateway.salvar(any(Produto.class))).thenReturn(produto);

        // Act
        Produto resultado = atualizarProdutoUsecase.desativar(id);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);
        assertThat(resultado.isAtivo()).isFalse();

        verify(produtoGateway).buscarPorId(id);
        verify(produtoGateway).salvar(produto);
        verify(kafkaTemplate).send(eq("produto-events"), any(CriarProdutoUsecase.ProdutoEventMessage.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado para desativar")
    void deveLancarExcecaoQuandoProdutoNaoEncontradoParaDesativar() {
        // Arrange
        Long id = 999L;
        when(produtoGateway.buscarPorId(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> atualizarProdutoUsecase.desativar(id))
                .isInstanceOf(ProdutoNaoEncontradoException.class)
                .hasMessage("Produto não encontrado com ID: " + id);

        verify(produtoGateway).buscarPorId(id);
        verify(produtoGateway, never()).salvar(any(Produto.class));
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("Deve continuar funcionando mesmo quando Kafka falha ao atualizar dados")
    void deveContinuarFuncionandoMesmoQuandoKafkaFalhaAoAtualizarDados() {
        // Arrange
        Long id = 1L;
        String novoNome = "Produto Atualizado";
        
        when(produtoGateway.buscarPorId(id)).thenReturn(Optional.of(produto));
        when(produtoGateway.salvar(any(Produto.class))).thenReturn(produto);
        when(kafkaTemplate.send(anyString(), any())).thenThrow(new RuntimeException("Kafka indisponível"));

        // Act
        Produto resultado = atualizarProdutoUsecase.atualizarDados(id, novoNome, "desc", new BigDecimal("10.00"), "cat");

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);

        verify(produtoGateway).buscarPorId(id);
        verify(produtoGateway).salvar(produto);
        verify(kafkaTemplate).send(eq("produto-events"), any(CriarProdutoUsecase.ProdutoEventMessage.class));
    }

    @Test
    @DisplayName("Deve continuar funcionando mesmo quando Kafka falha ao atualizar preço")
    void deveContinuarFuncionandoMesmoQuandoKafkaFalhaAoAtualizarPreco() {
        // Arrange
        Long id = 1L;
        BigDecimal novoPreco = new BigDecimal("99.99");
        
        when(produtoGateway.buscarPorId(id)).thenReturn(Optional.of(produto));
        when(produtoGateway.salvar(any(Produto.class))).thenReturn(produto);
        when(kafkaTemplate.send(anyString(), any())).thenThrow(new RuntimeException("Kafka indisponível"));

        // Act
        Produto resultado = atualizarProdutoUsecase.atualizarPreco(id, novoPreco);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);

        verify(produtoGateway).buscarPorId(id);
        verify(produtoGateway).salvar(produto);
        verify(kafkaTemplate).send(eq("produto-events"), any(CriarProdutoUsecase.ProdutoEventMessage.class));
    }

    @Test
    @DisplayName("Deve verificar se eventos Kafka são publicados com tipos corretos")
    void deveVerificarSeEventosKafkaSaoPublicadosComTiposCorretos() {
        // Arrange
        Long id = 1L;
        when(produtoGateway.buscarPorId(id)).thenReturn(Optional.of(produto));
        when(produtoGateway.salvar(any(Produto.class))).thenReturn(produto);

        // Act - Testar diferentes operações
        atualizarProdutoUsecase.atualizarDados(id, "Nome", "Desc", new BigDecimal("10.00"), "Cat");
        atualizarProdutoUsecase.atualizarPreco(id, new BigDecimal("20.00"));
        atualizarProdutoUsecase.ativar(id);
        atualizarProdutoUsecase.desativar(id);

        // Assert
        verify(kafkaTemplate, times(4)).send(eq("produto-events"), any(CriarProdutoUsecase.ProdutoEventMessage.class));
    }

    @Test
    @DisplayName("Deve normalizar dados ao atualizar produto")
    void deveNormalizarDadosAoAtualizarProduto() {
        // Arrange
        Long id = 1L;
        String nomeComEspacos = "  Produto com Espaços  ";
        String descricaoComEspacos = "  Descrição com espaços  ";
        String categoriaComEspacos = "  categoria  ";
        
        when(produtoGateway.buscarPorId(id)).thenReturn(Optional.of(produto));
        when(produtoGateway.salvar(any(Produto.class))).thenReturn(produto);

        // Act
        atualizarProdutoUsecase.atualizarDados(id, nomeComEspacos, descricaoComEspacos, new BigDecimal("10.00"), categoriaComEspacos);

        // Assert
        // Verificar se o produto foi atualizado com dados normalizados
        assertThat(produto.getNome()).isEqualTo("Produto com Espaços");
        assertThat(produto.getDescricao()).isEqualTo("Descrição com espaços");
        assertThat(produto.getCategoria()).isEqualTo("categoria");

        verify(produtoGateway).buscarPorId(id);
        verify(produtoGateway).salvar(produto);
    }
}