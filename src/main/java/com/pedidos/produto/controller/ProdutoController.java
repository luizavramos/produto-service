package com.pedidos.produto.controller;

import com.pedidos.produto.controller.json.ProdutoJson;
import com.pedidos.produto.domain.Produto;
import com.pedidos.produto.exception.SystemBaseException;
import com.pedidos.produto.usecase.AtualizarProdutoUsecase;
import com.pedidos.produto.usecase.BuscarProdutoUsecase;
import com.pedidos.produto.usecase.CriarProdutoUsecase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Produtos", description = "API para gerenciamento de produtos")
public class ProdutoController {

    private final CriarProdutoUsecase criarProdutoUsecase;
    private final BuscarProdutoUsecase buscarProdutoUsecase;
    private final AtualizarProdutoUsecase atualizarProdutoUsecase;

    @PostMapping
    @Operation(summary = "Criar produto", description = "Cria um novo produto no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProdutoJson.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou SKU já existe",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> criarProduto(@Valid @RequestBody @Parameter(description = "Dados do produto a ser criado") ProdutoJson produtoJson) {
        try {
            Produto produto = criarProdutoUsecase.executar(
                    produtoJson.getNome(),
                    produtoJson.getSku(),
                    produtoJson.getDescricao(),
                    produtoJson.getPreco(),
                    produtoJson.getCategoria()
            );

            ProdutoJson response = ProdutoJson.fromDomain(produto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (SystemBaseException e) {
            log.warn("Erro de negócio ao criar produto: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Dados inválidos ao criar produto: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro interno ao criar produto: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Erro interno do servidor"));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Busca um produto específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProdutoJson.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> buscarProdutoPorId(@PathVariable @Parameter(description = "ID do produto", example = "1") Long id) {
        try {
            Produto produto = buscarProdutoUsecase.buscarPorId(id);
            ProdutoJson response = ProdutoJson.fromDomain(produto);
            return ResponseEntity.ok(response);

        } catch (SystemBaseException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro interno ao buscar produto por ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Erro interno do servidor"));
        }
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Buscar produto por SKU", description = "Busca um produto específico pelo seu SKU")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProdutoJson.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> buscarProdutoPorSku(@PathVariable @Parameter(description = "SKU do produto", example = "PROD-001") String sku) {
        try {
            Produto produto = buscarProdutoUsecase.buscarPorSku(sku);
            ProdutoJson response = ProdutoJson.fromDomain(produto);
            return ResponseEntity.ok(response);

        } catch (SystemBaseException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro interno ao buscar produto por SKU {}: {}", sku, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Erro interno do servidor"));
        }
    }

    @GetMapping
    @Operation(summary = "Listar produtos", description = "Lista produtos com filtros opcionais")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProdutoJson.class)))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> listarProdutos(
            @RequestParam(required = false) @Parameter(description = "Filtrar por categoria", example = "ELETRÔNICOS") String categoria,
            @RequestParam(required = false) @Parameter(description = "Listar apenas produtos ativos", example = "true") Boolean apenasAtivos,
            @RequestParam(required = false) @Parameter(description = "Preço mínimo", example = "10.00") BigDecimal precoMin,
            @RequestParam(required = false) @Parameter(description = "Preço máximo", example = "100.00") BigDecimal precoMax) {
        try {
            List<Produto> produtos;

            if (categoria != null && !categoria.trim().isEmpty()) {
                produtos = buscarProdutoUsecase.buscarPorCategoria(categoria);
            } else if (precoMin != null || precoMax != null) {
                produtos = buscarProdutoUsecase.buscarPorFaixaPreco(precoMin, precoMax);
            } else if (Boolean.TRUE.equals(apenasAtivos)) {
                produtos = buscarProdutoUsecase.buscarAtivos();
            } else {
                produtos = buscarProdutoUsecase.buscarTodos();
            }

            List<ProdutoJson> response = produtos.stream()
                    .map(ProdutoJson::fromDomain)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro interno ao listar produtos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Erro interno do servidor"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto", description = "Atualiza os dados de um produto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProdutoJson.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> atualizarProduto(
            @PathVariable @Parameter(description = "ID do produto", example = "1") Long id,
            @Valid @RequestBody @Parameter(description = "Novos dados do produto") ProdutoJson produtoJson) {
        try {
            Produto produto = atualizarProdutoUsecase.atualizarDados(
                    id,
                    produtoJson.getNome(),
                    produtoJson.getDescricao(),
                    produtoJson.getPreco(),
                    produtoJson.getCategoria()
            );

            ProdutoJson response = ProdutoJson.fromDomain(produto);
            return ResponseEntity.ok(response);

        } catch (SystemBaseException e) {
            HttpStatus status = e instanceof com.pedidos.produto.exception.ProdutoNaoEncontradoException
                    ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(new ErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro interno ao atualizar produto ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Erro interno do servidor"));
        }
    }

    @PatchMapping("/{id}/preco")
    @Operation(summary = "Atualizar preço", description = "Atualiza apenas o preço de um produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preço atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProdutoJson.class))),
            @ApiResponse(responseCode = "400", description = "Preço inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> atualizarPreco(
            @PathVariable @Parameter(description = "ID do produto", example = "1") Long id,
            @RequestBody @Valid @Parameter(description = "Novo preço do produto") PrecoRequest precoRequest) {
        try {
            Produto produto = atualizarProdutoUsecase.atualizarPreco(id, precoRequest.getPreco());
            ProdutoJson response = ProdutoJson.fromDomain(produto);
            return ResponseEntity.ok(response);

        } catch (SystemBaseException e) {
            HttpStatus status = e instanceof com.pedidos.produto.exception.ProdutoNaoEncontradoException
                    ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(new ErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro interno ao atualizar preço do produto ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Erro interno do servidor"));
        }
    }

    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Ativar produto", description = "Ativa um produto inativo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto ativado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProdutoJson.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> ativarProduto(@PathVariable @Parameter(description = "ID do produto", example = "1") Long id) {
        try {
            Produto produto = atualizarProdutoUsecase.ativar(id);
            ProdutoJson response = ProdutoJson.fromDomain(produto);
            return ResponseEntity.ok(response);

        } catch (SystemBaseException e) {
            HttpStatus status = e instanceof com.pedidos.produto.exception.ProdutoNaoEncontradoException
                    ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro interno ao ativar produto ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Erro interno do servidor"));
        }
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativar produto", description = "Desativa um produto ativo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto desativado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProdutoJson.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> desativarProduto(@PathVariable @Parameter(description = "ID do produto", example = "1") Long id) {
        try {
            Produto produto = atualizarProdutoUsecase.desativar(id);
            ProdutoJson response = ProdutoJson.fromDomain(produto);
            return ResponseEntity.ok(response);

        } catch (SystemBaseException e) {
            HttpStatus status = e instanceof com.pedidos.produto.exception.ProdutoNaoEncontradoException
                    ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro interno ao desativar produto ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Erro interno do servidor"));
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Obter estatísticas", description = "Retorna estatísticas gerais dos produtos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatisticsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> obterEstatisticas() {
        try {
            long totalProdutos = buscarProdutoUsecase.contarProdutos();
            long produtosAtivos = buscarProdutoUsecase.contarProdutosAtivos();

            StatisticsResponse stats = new StatisticsResponse(totalProdutos, produtosAtivos);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Erro interno ao obter estatísticas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Erro interno do servidor"));
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @Schema(description = "Request para atualização de preço")
    public static class PrecoRequest {
        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.0", inclusive = true, message = "Preço não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "Preço deve ter no máximo 8 dígitos inteiros e 2 decimais")
        @Schema(description = "Novo preço do produto", example = "99.99", required = true)
        private BigDecimal preco;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @Schema(description = "Estatísticas dos produtos")
    public static class StatisticsResponse {
        @Schema(description = "Número total de produtos cadastrados", example = "150")
        private long totalProdutos;
        
        @Schema(description = "Número de produtos ativos", example = "120")
        private long produtosAtivos;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @Schema(description = "Resposta de erro padrão")
    public static class ErrorResponse {
        @Schema(description = "Mensagem de erro", example = "Produto não encontrado")
        private String message;
        
        @Schema(description = "Timestamp do erro em milissegundos", example = "1642248600000")
        private long timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }
}