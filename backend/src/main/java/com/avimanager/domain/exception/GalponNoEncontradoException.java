package com.avimanager.domain.exception;

public class GalponNoEncontradoException extends RuntimeException {

    public GalponNoEncontradoException(String galponId) {
        super("No existe un galpón con id " + galponId);
    }
}
