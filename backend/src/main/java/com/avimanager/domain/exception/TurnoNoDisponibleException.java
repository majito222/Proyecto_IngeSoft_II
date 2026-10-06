package com.avimanager.domain.exception;

/**
 * El turno del dia no existe o ya no esta abierto, asi que no se puede cerrar.
 */
public class TurnoNoDisponibleException extends RuntimeException {

    public TurnoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
