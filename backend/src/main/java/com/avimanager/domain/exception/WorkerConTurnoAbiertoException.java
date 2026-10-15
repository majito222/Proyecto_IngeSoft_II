package com.avimanager.domain.exception;

/**
 * F-11: un worker con un turno abierto no se reasigna, porque el turno quedaria
 * sobre un galpon que ya no es el suyo (trazabilidad de F-10).
 */
public class WorkerConTurnoAbiertoException extends RuntimeException {

    public WorkerConTurnoAbiertoException(String username) {
        super("El worker " + username + " tiene un turno abierto. Debe cerrarlo antes de cambiar de galpón.");
    }
}
