package com.avimanager.domain.model;

/**
 * Usuario del sistema (F-09). Solo guarda el hash de la contraseña: el
 * algoritmo de cifrado lo decide la infraestructura (CifradorContrasenaPort).
 */
public class Usuario {

    private final String id;
    private final String username;
    private final String nombreCompleto;
    private final String contrasenaCifrada;
    private final Rol rol;
    private final boolean activo;

    public Usuario(String id, String username, String nombreCompleto, String contrasenaCifrada, Rol rol, boolean activo) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El usuario debe tener un nombre de usuario");
        }
        if (rol == null) {
            throw new IllegalArgumentException("El usuario debe tener un rol");
        }
        this.id = id;
        this.username = username;
        this.nombreCompleto = nombreCompleto;
        this.contrasenaCifrada = contrasenaCifrada;
        this.rol = rol;
        this.activo = activo;
    }

    public boolean tienePermiso(Permiso permiso) {
        return rol.tienePermiso(permiso);
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getContrasenaCifrada() {
        return contrasenaCifrada;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
