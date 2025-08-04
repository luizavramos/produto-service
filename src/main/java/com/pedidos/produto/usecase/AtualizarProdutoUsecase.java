package com.pedidos.produto.usecase;

import com.pedidos.produto.domain.Produto;
import com.pedidos.produto.exception.ProdutoNaoEncontradoException;
import com.pedidos.produto.gateway.ProdutoGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class AtualizarProdutoUsecase {
    private final ProdutoGateway produtoGateway;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PRODUTO_TOPIC = "produto-events";

    public Produto atualizarDados(Long id, String nome, String descricao, BigDecimal preco, String categoria) {
        log.info("Atualizando dados do produto ID: {}", id);

        Produto produto = produtoGateway.buscarPorId(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado com ID: " + id));

        produto.atualizarDados(nome, descricao, preco, categoria);

        Produto produtoAtualizado = produtoGateway.salvar(produto);

        // Publicar evento no Kafka
        publicarEventoKafka("PRODUTO_ATUALIZADO", produtoAtualizado);

        log.info("Produto atualizado com sucesso. ID: {}", produtoAtualizado.getId());
        return produtoAtualizado;
    }

    public Produto atualizarPreco(Long id, BigDecimal novoPreco) {
        log.info("Atualizando preço do produto ID: {} para {}", id, novoPreco);

        Produto produto = produtoGateway.buscarPorId(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado com ID: " + id));

        produto.atualizarPreco(novoPreco);

        Produto produtoAtualizado = produtoGateway.salvar(produto);

        // Publicar evento no Kafka
        publicarEventoKafka("PRODUTO_PRECO_ATUALIZADO", produtoAtualizado);

        log.info("Preço do produto atualizado com sucesso. ID: {}, Novo preço: {}",
                produtoAtualizado.getId(), produtoAtualizado.getPreco());
        return produtoAtualizado;
    }

    public Produto ativar(Long id) {
        log.info("Ativando produto ID: {}", id);

        Produto produto = produtoGateway.buscarPorId(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado com ID: " + id));

        produto.ativar();

        Produto produtoAtualizado = produtoGateway.salvar(produto);

        // Publicar evento no Kafka
        publicarEventoKafka("PRODUTO_ATIVADO", produtoAtualizado);

        log.info("Produto ativado com sucesso. ID: {}", produtoAtualizado.getId());
        return produtoAtualizado;
    }

    public Produto desativar(Long id) {
        log.info("Desativando produto ID: {}", id);

        Produto produto = produtoGateway.buscarPorId(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado com ID: " + id));

        produto.desativar();

        Produto produtoAtualizado = produtoGateway.salvar(produto);

        // Publicar evento no Kafka
        publicarEventoKafka("PRODUTO_DESATIVADO", produtoAtualizado);

        log.info("Produto desativado com sucesso. ID: {}", produtoAtualizado.getId());
        return produtoAtualizado;
    }

    private void publicarEventoKafka(String tipoEvento, Produto produto) {
        try {
            CriarProdutoUsecase.ProdutoEventMessage evento = CriarProdutoUsecase.ProdutoEventMessage.builder()
                    .tipoEvento(tipoEvento)
                    .produtoId(produto.getId())
                    .sku(produto.getSku())
                    .nome(produto.getNome())
                    .preco(produto.getPreco())
                    .ativo(produto.isAtivo())
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send(PRODUTO_TOPIC, evento);
            log.debug("Evento Kafka publicado: {}", evento);
        } catch (Exception e) {
            log.error("Erro ao publicar evento Kafka: {}", e.getMessage(), e);
        }
    }
}
