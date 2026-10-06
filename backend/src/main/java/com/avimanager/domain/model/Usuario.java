package com.avimanager.domain.model;

/**
 * Usuario del sistema (F-09). Solo guarda el hash de la contraseña: el
 * algoritmo de cifrado lo decide la infraestructura (CifradorContrasenaPort).
 * Un worker puede tener un galpon asignado (RQ-007), donde inicia su turno (F-10).
 */
public class Usuario {

    private final String id;
    private final String username;
    private final String nombreCompleto;
    private final String contrasenaCifrada;
    private final Rol rol;
    private final boolean activo;
    private final String galponAsignadoId;

    public Usuario(String id, String username, String nombreCompleto, String contrasenaCifrada, Rol rol, boolean activo) {
        this(id, username, nombreCompleto, contrasenaCifrada, rol, activo, null);
    }

    public Usuario(String id, String username, String nombreCompleto, String contrasenaCifrada, Rol rol, boolean activo,
                   String galponAsignadoId) {
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
        this.galponAsignadoId = galponAsignadoId;
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

    public String getGalponAsignadoId() {
        return galponAsignadoId;
    }
}
