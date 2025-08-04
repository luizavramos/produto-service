package com.pedidos.produto.exception;

public class ErroAoAcessarRepositorioException  extends SystemBaseException {

    private static final long serialVersionUID = 1L;

    public ErroAoAcessarRepositorioException(String message) {
        super(message);
    }

    public ErroAoAcessarRepositorioException(String message, Throwable cause) {
        super(message, cause);
    }
}

