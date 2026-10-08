package com.avimanager.domain.exception;

public class AlertaNoEncontradaException extends RuntimeException {

    public AlertaNoEncontradaException(String alertaId) {
        super("No existe una alerta sanitaria con id " + alertaId);
    }
}
