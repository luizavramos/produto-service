package com.pedidos.produto.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Produto {
    @EqualsAndHashCode.Include
    private Long id;

    private String nome;

    @EqualsAndHashCode.Include
    private String sku;

    private String descricao;
    private BigDecimal preco;
    private String categoria;
    private Boolean ativo;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Produto(String nome, String sku, String descricao, BigDecimal preco, String categoria) {
        this();
        validarNome(nome);
        validarSku(sku);
        validarPreco(preco);

        this.nome = nome.trim();
        this.sku = sku.toUpperCase().trim();
        this.descricao = descricao != null ? descricao.trim() : null;
        this.preco = preco;
        this.categoria = categoria != null ? categoria.trim() : null;
        this.ativo = true;
    }

    private void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        }
        if (nome.trim().length() < 2) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 2 caracteres");
        }
        if (nome.trim().length() > 255) {
            throw new IllegalArgumentException("Nome deve ter no máximo 255 caracteres");
        }
    }

    private void validarSku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU é obrigatório");
        }
        if (!sku.matches("^[A-Z0-9-_]{3,50}$")) {
            throw new IllegalArgumentException("SKU deve conter apenas letras maiúsculas, números, hífen e underscore (3-50 caracteres)");
        }
    }

    private void validarPreco(BigDecimal preco) {
        if (preco == null) {
            throw new IllegalArgumentException("Preço é obrigatório");
        }
        if (preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo");
        }
        if (preco.scale() > 2) {
            throw new IllegalArgumentException("Preço deve ter no máximo 2 casas decimais");
        }
    }

    public void atualizarPreco(BigDecimal novoPreco) {
        validarPreco(novoPreco);
        this.preco = novoPreco;
        this.updatedAt = LocalDateTime.now();
    }

    public void atualizarDados(String nome, String descricao, BigDecimal preco, String categoria) {
        validarNome(nome);
        validarPreco(preco);

        this.nome = nome.trim();
        this.descricao = descricao != null ? descricao.trim() : null;
        this.preco = preco;
        this.categoria = categoria != null ? categoria.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    public void ativar() {
        this.ativo = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void desativar() {
        this.ativo = false;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAtivo() {
        return this.ativo != null && this.ativo;
    }

    public String getPrecoFormatado() {
        return "R$ " + preco.toString().replace(".", ",");
    }

    public String getSkuComCategoria() {
        if (categoria != null && !categoria.isEmpty()) {
            return categoria.toUpperCase() + "-" + sku;
        }
        return sku;
    }
}
