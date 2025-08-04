package com.pedidos.produto.gateway.jpa;

import com.pedidos.produto.domain.Produto;
import com.pedidos.produto.exception.ErroAoAcessarRepositorioException;
import com.pedidos.produto.gateway.ProdutoGateway;
import com.pedidos.produto.gateway.jpa.entity.ProdutoEntity;
import com.pedidos.produto.gateway.jpa.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProdutoJpaGateway implements ProdutoGateway {

    private final ProdutoRepository produtoRepository;

    @Override
    public Produto salvar(Produto produto) {
        try {
            ProdutoEntity entity = toEntity(produto);
            ProdutoEntity entitySalva = produtoRepository.save(entity);
            log.debug("Produto salvo no banco: ID {}", entitySalva.getId());
            return toDomain(entitySalva);
        } catch (Exception e) {
            log.error("Erro ao salvar produto: {}", e.getMessage(), e);
            throw new ErroAoAcessarRepositorioException("Erro ao salvar produto", e);
        }
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        try {
            return produtoRepository.findById(id)
                    .map(this::toDomain);
        } catch (Exception e) {
            log.error("Erro ao buscar produto por ID {}: {}", id, e.getMessage(), e);
            throw new ErroAoAcessarRepositorioException("Erro ao buscar produto por ID", e);
        }
    }

    @Override
    public Optional<Produto> buscarPorSku(String sku) {
        try {
            return produtoRepository.findBySku(sku)
                    .map(this::toDomain);
        } catch (Exception e) {
            log.error("Erro ao buscar produto por SKU {}: {}", sku, e.getMessage(), e);
            throw new ErroAoAcessarRepositorioException("Erro ao buscar produto por SKU", e);
        }
    }

    @Override
    public List<Produto> buscarTodos() {
        try {
            return produtoRepository.findAll()
                    .stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erro ao buscar todos os produtos: {}", e.getMessage(), e);
            throw new ErroAoAcessarRepositorioException("Erro ao buscar todos os produtos", e);
        }
    }

    @Override
    public List<Produto> buscarPorCategoria(String categoria) {
        try {
            return produtoRepository.findByCategoriaIgnoreCaseAndAtivoTrue(categoria)
                    .stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erro ao buscar produtos por categoria {}: {}", categoria, e.getMessage(), e);
            throw new ErroAoAcessarRepositorioException("Erro ao buscar produtos por categoria", e);
        }
    }

    @Override
    public List<Produto> buscarAtivos() {
        try {
            return produtoRepository.findByAtivoTrue()
                    .stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erro ao buscar produtos ativos: {}", e.getMessage(), e);
            throw new ErroAoAcessarRepositorioException("Erro ao buscar produtos ativos", e);
        }
    }

    @Override
    public List<Produto> buscarPorFaixaPreco(BigDecimal precoMin, BigDecimal precoMax) {
        try {
            return produtoRepository.findByFaixaPrecoOpcional(precoMin, precoMax)
                    .stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erro ao buscar produtos por faixa de preço: {}", e.getMessage(), e);
            throw new ErroAoAcessarRepositorioException("Erro ao buscar produtos por faixa de preço", e);
        }
    }

    @Override
    public void deletar(Long id) {
        try {
            produtoRepository.deleteById(id);
            log.debug("Produto deletado: ID {}", id);
        } catch (Exception e) {
            log.error("Erro ao deletar produto ID {}: {}", id, e.getMessage(), e);
            throw new ErroAoAcessarRepositorioException("Erro ao deletar produto", e);
        }
    }

    @Override
    public boolean existePorSku(String sku) {
        try {
            return produtoRepository.existsBySku(sku);
        } catch (Exception e) {
            log.error("Erro ao verificar existência de SKU {}: {}", sku, e.getMessage(), e);
            throw new ErroAoAcessarRepositorioException("Erro ao verificar existência de SKU", e);
        }
    }

    @Override
    public long contarProdutos() {
        try {
            return produtoRepository.count();
        } catch (Exception e) {
            log.error("Erro ao contar produtos: {}", e.getMessage(), e);
            throw new ErroAoAcessarRepositorioException("Erro ao contar produtos", e);
        }
    }

    @Override
    public long contarProdutosAtivos() {
        try {
            return produtoRepository.countByAtivoTrue();
        } catch (Exception e) {
            log.error("Erro ao contar produtos ativos: {}", e.getMessage(), e);
            throw new ErroAoAcessarRepositorioException("Erro ao contar produtos ativos", e);
        }
    }

    // Métodos de conversão Entity <-> Domain
    private ProdutoEntity toEntity(Produto produto) {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(produto.getId());
        entity.setNome(produto.getNome());
        entity.setSku(produto.getSku());
        entity.setDescricao(produto.getDescricao());
        entity.setPreco(produto.getPreco());
        entity.setCategoria(produto.getCategoria());
        entity.setAtivo(produto.getAtivo());
        entity.setCreatedAt(produto.getCreatedAt());
        entity.setUpdatedAt(produto.getUpdatedAt());
        return entity;
    }

    private Produto toDomain(ProdutoEntity entity) {
        Produto produto = new Produto();
        produto.setId(entity.getId());
        produto.setNome(entity.getNome());
        produto.setSku(entity.getSku());
        produto.setDescricao(entity.getDescricao());
        produto.setPreco(entity.getPreco());
        produto.setCategoria(entity.getCategoria());
        produto.setAtivo(entity.getAtivo());
        produto.setCreatedAt(entity.getCreatedAt());
        produto.setUpdatedAt(entity.getUpdatedAt());
        return produto;
    }
}
