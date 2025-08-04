package com.pedidos.produto.usecase;

import com.pedidos.produto.domain.Produto;
import com.pedidos.produto.exception.SkuJaExisteException;
import com.pedidos.produto.gateway.ProdutoGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CriarProdutoUsecase {
    private final ProdutoGateway produtoGateway;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PRODUTO_TOPIC = "produto-events";

    public Produto executar(String nome, String sku, String descricao, BigDecimal preco, String categoria) {
        log.info("Iniciando criação de produto com SKU: {}", sku);

        if (produtoGateway.existePorSku(sku)) {
            throw new SkuJaExisteException("SKU já cadastrado no sistema: " + sku);
        }

        Produto produto = new Produto(nome, sku, descricao, preco, categoria);

        Produto produtoSalvo = produtoGateway.salvar(produto);

        publicarEventoKafka("PRODUTO_CRIADO", produtoSalvo);

        log.info("Produto criado com sucesso. ID: {}, SKU: {}", produtoSalvo.getId(), produtoSalvo.getSku());
        return produtoSalvo;
}
    private void publicarEventoKafka(String tipoEvento, Produto produto) {
        try {
            ProdutoEventMessage evento = ProdutoEventMessage.builder()
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

    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ProdutoEventMessage {
        private String tipoEvento;
        private Long produtoId;
        private String sku;
        private String nome;
        private BigDecimal preco;
        private Boolean ativo;
        private Long timestamp;
    }
}
