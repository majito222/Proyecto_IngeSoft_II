package com.avimanager.domain.exception;

public class InsumoNoEncontradoException extends RuntimeException {

    public InsumoNoEncontradoException(String insumoId) {
        super("No existe un insumo con id " + insumoId);
    }
}
