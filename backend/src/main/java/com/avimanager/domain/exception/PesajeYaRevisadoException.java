package com.avimanager.domain.exception;

import com.avimanager.domain.model.EstadoPesaje;

/**
 * Solo un pesaje PENDIENTE_REVISION se revisa, y una sola vez (F-07).
 */
public class PesajeYaRevisadoException extends RuntimeException {

    public PesajeYaRevisadoException(String pesajeId, EstadoPesaje estado) {
        super("El pesaje " + pesajeId + " no está pendiente de revisión (estado " + estado + ")");
    }
}
