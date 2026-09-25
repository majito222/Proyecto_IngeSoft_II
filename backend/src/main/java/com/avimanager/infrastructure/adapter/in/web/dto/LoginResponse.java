package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.port.in.SesionIniciada;

public class LoginResponse {

    private final String token;
    private final UsuarioResponse usuario;

    private LoginResponse(String token, UsuarioResponse usuario) {
        this.token = token;
        this.usuario = usuario;
    }

    public static LoginResponse desde(SesionIniciada sesion) {
        return new LoginResponse(sesion.getToken(), UsuarioResponse.desde(sesion.getUsuario()));
    }

    public String getToken() {
        return token;
    }

    public UsuarioResponse getUsuario() {
        return usuario;
    }
}
