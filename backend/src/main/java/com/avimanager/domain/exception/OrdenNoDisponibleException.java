package com.avimanager.domain.exception;

/**
 * La orden de mantenimiento no admite esa transicion en su estado actual (F-08.3).
 */
public class OrdenNoDisponibleException extends RuntimeException {

    public OrdenNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
