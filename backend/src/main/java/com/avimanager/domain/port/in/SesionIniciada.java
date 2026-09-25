package com.avimanager.domain.port.in;

import com.avimanager.domain.model.Usuario;

public class SesionIniciada {

    private final String token;
    private final Usuario usuario;

    public SesionIniciada(String token, Usuario usuario) {
        this.token = token;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
