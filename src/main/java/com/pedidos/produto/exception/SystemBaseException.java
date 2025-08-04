package com.pedidos.produto.exception;

public class SystemBaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SystemBaseException(String message) {
        super(message);
    }

    public SystemBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
