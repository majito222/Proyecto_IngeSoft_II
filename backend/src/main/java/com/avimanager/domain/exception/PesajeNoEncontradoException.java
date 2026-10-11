package com.avimanager.domain.exception;

public class PesajeNoEncontradoException extends RuntimeException {

    public PesajeNoEncontradoException(String pesajeId) {
        super("No existe el pesaje " + pesajeId);
    }
}
