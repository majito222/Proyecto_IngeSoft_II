package com.avimanager.domain.exception;

import java.time.LocalDate;

/**
 * Un lote tiene un solo turno por dia (F-10).
 */
public class TurnoYaIniciadoException extends RuntimeException {

    public TurnoYaIniciadoException(String loteId, LocalDate fecha) {
        super("El lote " + loteId + " ya tiene un turno iniciado el " + fecha);
    }
}
