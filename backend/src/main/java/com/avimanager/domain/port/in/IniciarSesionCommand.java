package com.avimanager.domain.port.in;

public class IniciarSesionCommand {

    private final String username;
    private final String contrasena;

    public IniciarSesionCommand(String username, String contrasena) {
        this.username = username;
        this.contrasena = contrasena;
    }

    public String getUsername() {
        return username;
    }

    public String getContrasena() {
        return contrasena;
    }
}
