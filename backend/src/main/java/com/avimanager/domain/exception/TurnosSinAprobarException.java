package com.avimanager.domain.exception;

import java.time.LocalDate;

/**
 * F-04.3: la consolidacion del dia se rechaza mientras quede algun turno de esa
 * fecha sin aprobar.
 */
public class TurnosSinAprobarException extends RuntimeException {

    public TurnosSinAprobarException(LocalDate fecha, long pendientes) {
        super("No se puede consolidar el " + fecha + ": " + (pendientes == 1 ? "queda 1 turno" : "quedan "
                + pendientes + " turnos") + " sin aprobar");
    }
}
