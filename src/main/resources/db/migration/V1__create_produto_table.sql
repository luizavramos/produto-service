CREATE TABLE IF NOT EXISTS tb_produto (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    sku VARCHAR(50) NOT NULL UNIQUE,
    descricao TEXT,
    preco DECIMAL(10,2) NOT NULL,
    categoria VARCHAR(100),
    ativo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_produto_sku ON tb_produto(sku);
CREATE INDEX IF NOT EXISTS idx_produto_categoria ON tb_produto(categoria);
CREATE INDEX IF NOT EXISTS idx_produto_ativo ON tb_produto(ativo);
CREATE INDEX IF NOT EXISTS idx_produto_preco ON tb_produto(preco);
CREATE INDEX IF NOT EXISTS idx_produto_nome ON tb_produto(nome);

COMMENT ON TABLE tb_produto IS 'Tabela de produtos do sistema';
COMMENT ON COLUMN tb_produto.sku IS 'SKU único do produto (Stock Keeping Unit)';
COMMENT ON COLUMN tb_produto.preco IS 'Preço do produto com 2 casas decimais';
COMMENT ON COLUMN tb_produto.ativo IS 'Indica se o produto está ativo/disponível';