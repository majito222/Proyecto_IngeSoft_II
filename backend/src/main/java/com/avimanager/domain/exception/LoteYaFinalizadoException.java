package com.avimanager.domain.exception;

public class LoteYaFinalizadoException extends RuntimeException {

    public LoteYaFinalizadoException(String loteId) {
        super("El lote " + loteId + " ya está finalizado");
    }
}
