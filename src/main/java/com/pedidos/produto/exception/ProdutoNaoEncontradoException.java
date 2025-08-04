package com.pedidos.produto.exception;

public class ProdutoNaoEncontradoException extends SystemBaseException {

    private static final long serialVersionUID = 1L;

    public ProdutoNaoEncontradoException(String message) {
        super(message);
    }
}
