package com.avimanager.domain.exception;

public class OrdenNoEncontradaException extends RuntimeException {

    public OrdenNoEncontradaException(String ordenId) {
        super("No existe la orden de mantenimiento " + ordenId);
    }
}
