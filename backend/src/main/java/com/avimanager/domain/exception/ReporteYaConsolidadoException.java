package com.avimanager.domain.exception;

import java.time.LocalDate;

/** F-04.1: cada fecha se consolida una sola vez. */
public class ReporteYaConsolidadoException extends RuntimeException {

    public ReporteYaConsolidadoException(LocalDate fecha) {
        super("El reporte del " + fecha + " ya está consolidado");
    }
}
