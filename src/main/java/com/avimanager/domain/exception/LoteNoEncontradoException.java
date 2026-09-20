package com.avimanager.domain.exception;

public class LoteNoEncontradoException extends RuntimeException {

    public LoteNoEncontradoException(String loteId) {
        super("No existe un lote con id " + loteId);
    }
}
