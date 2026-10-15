package com.avimanager.domain.exception;

import com.avimanager.domain.model.Rol;

/**
 * F-11: solo los operarios (workers) se asignan a un galpon.
 */
public class UsuarioNoEsWorkerException extends RuntimeException {

    public UsuarioNoEsWorkerException(String username, Rol rol) {
        super("El usuario " + username + " es " + rol.getDescripcion()
                + "; solo los operarios se asignan a un galpón");
    }
}
