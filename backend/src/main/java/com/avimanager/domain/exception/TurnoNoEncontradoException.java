package com.avimanager.domain.exception;

public class TurnoNoEncontradoException extends RuntimeException {

    public TurnoNoEncontradoException(String turnoId) {
        super("No existe un turno con id " + turnoId);
    }
}
