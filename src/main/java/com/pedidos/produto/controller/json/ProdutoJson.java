package com.pedidos.produto.controller.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pedidos.produto.domain.Produto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Schema(description = "Representação JSON de um produto")
public class ProdutoJson {

    @Schema(description = "ID único do produto", example = "1")
    private Long id;

    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    @Schema(description = "Nome do produto", example = "Smartphone Samsung Galaxy", required = true)
    private String nome;

    @NotBlank(message = "SKU é obrigatório")
    @Pattern(regexp = "^[A-Z0-9-_]{3,50}$",
            message = "SKU deve conter apenas letras maiúsculas, números, hífen e underscore (3-50 caracteres)")
    @Schema(description = "Código SKU do produto", example = "SMARTPHONE-GALAXY-S23", required = true)
    private String sku;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    @Schema(description = "Descrição detalhada do produto", example = "Smartphone com tela de 6.1 polegadas, 128GB de armazenamento")
    private String descricao;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Preço não pode ser negativo")
    @Digits(integer = 8, fraction = 2, message = "Preço deve ter no máximo 8 dígitos inteiros e 2 decimais")
    @Schema(description = "Preço do produto", example = "1299.99", required = true)
    private BigDecimal preco;

    @Size(max = 100, message = "Categoria deve ter no máximo 100 caracteres")
    @Schema(description = "Categoria do produto", example = "ELETRÔNICOS")
    private String categoria;

    @Schema(description = "Indica se o produto está ativo", example = "true")
    private Boolean ativo;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data e hora de criação do produto", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data e hora da última atualização", example = "2024-01-16T14:20:00")
    private LocalDateTime updatedAt;

    // Campos calculados
    @Schema(description = "Preço formatado para exibição", example = "R$ 1.299,99")
    private String precoFormatado;
    
    @Schema(description = "SKU combinado com categoria", example = "ELETRÔNICOS-SMARTPHONE-GALAXY-S23")
    private String skuComCategoria;

    public static ProdutoJson fromDomain(Produto produto) {
        ProdutoJsonBuilder builder = ProdutoJson.builder()
                .id(produto.getId())
                .nome(produto.getNome())
                .sku(produto.getSku())
                .descricao(produto.getDescricao())
                .preco(produto.getPreco())
                .categoria(produto.getCategoria())
                .ativo(produto.getAtivo())
                .createdAt(produto.getCreatedAt())
                .updatedAt(produto.getUpdatedAt())
                .precoFormatado(produto.getPrecoFormatado())
                .skuComCategoria(produto.getSkuComCategoria());

        return builder.build();
    }

    public Produto toDomain() {
        return new Produto(this.nome, this.sku, this.descricao, this.preco, this.categoria);
    }
}