package com.avimanager.domain.exception;

/**
 * Una alerta sanitaria se atiende una sola vez (F-02.5).
 */
public class AlertaYaAtendidaException extends RuntimeException {

    public AlertaYaAtendidaException(String alertaId) {
        super("La alerta " + alertaId + " ya fue atendida");
    }
}
