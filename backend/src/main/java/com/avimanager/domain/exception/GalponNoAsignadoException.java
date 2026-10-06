package com.avimanager.domain.exception;

/**
 * El worker no tiene galpon asignado, o su galpon no aloja un lote, asi que no
 * puede iniciar turno.
 */
public class GalponNoAsignadoException extends RuntimeException {

    public GalponNoAsignadoException(String mensaje) {
        super(mensaje);
    }
}
