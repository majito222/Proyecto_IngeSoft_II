package com.avimanager.domain.port.out;

import com.avimanager.domain.model.Usuario;

/**
 * Emite el token de sesion de un usuario autenticado (JWT en la infraestructura actual).
 */
public interface GeneradorTokenPort {

    String generarToken(Usuario usuario);
}
