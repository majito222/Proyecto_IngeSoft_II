package com.avimanager.domain.exception;

/**
 * Solo el responsable del turno puede cerrarlo, aunque otro usuario tenga el
 * permiso CERRAR_TURNO (incluido el Administrador): asi cada reporte queda a
 * nombre de quien realmente lo registro (trazabilidad, F-09 / F-10).
 */
public class TurnoAjenoException extends RuntimeException {

    public TurnoAjenoException(String responsableUsername) {
        super("Este turno es de " + responsableUsername + ". Solo su responsable puede cerrarlo.");
    }
}
