package com.avimanager.domain.exception;

/**
 * Se usa el mismo mensaje para usuario inexistente, contraseña incorrecta o
 * usuario inactivo, para no revelar cuales usuarios existen.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos");
    }
}
