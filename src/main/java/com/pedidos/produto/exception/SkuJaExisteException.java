package com.pedidos.produto.exception;

public class SkuJaExisteException extends SystemBaseException {

    private static final long serialVersionUID = 1L;

    public SkuJaExisteException(String message) {
        super(message);
    }
}