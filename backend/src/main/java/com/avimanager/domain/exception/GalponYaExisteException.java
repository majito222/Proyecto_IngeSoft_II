package com.avimanager.domain.exception;

/**
 * Dos galpones no pueden llamarse igual: el nombre es como los identifican los
 * workers y el resto de roles en todas las pantallas (F-11).
 */
public class GalponYaExisteException extends RuntimeException {

    public GalponYaExisteException(String nombre) {
        super("Ya existe un galpón llamado \"" + nombre + "\"");
    }
}
