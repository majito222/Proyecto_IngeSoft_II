package com.avimanager.domain.exception;

/**
 * Un worker solo reporta danos en su galpon asignado (F-08.2).
 */
public class GalponAjenoException extends RuntimeException {

    public GalponAjenoException(String mensaje) {
        super(mensaje);
    }
}
