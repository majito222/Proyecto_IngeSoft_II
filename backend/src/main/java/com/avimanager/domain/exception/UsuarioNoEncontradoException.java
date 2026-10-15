package com.avimanager.domain.exception;

public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(String username) {
        super("No existe el usuario " + username);
    }
}
