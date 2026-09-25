package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.model.Permiso;
import com.avimanager.domain.model.Rol;
import com.avimanager.domain.model.Usuario;

import java.util.List;

/**
 * Datos del usuario autenticado que necesita el frontend para mostrar su
 * nombre y armar el menu segun sus permisos.
 */
public class UsuarioResponse {

    private final String username;
    private final String nombreCompleto;
    private final String rol;
    private final String rolDescripcion;
    private final List<String> permisos;

    public UsuarioResponse(String username, String nombreCompleto, Rol rol) {
        this.username = username;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol.name();
        this.rolDescripcion = rol.getDescripcion();
        this.permisos = rol.getPermisos().stream().map(Permiso::name).sorted().toList();
    }

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getUsername(), usuario.getNombreCompleto(), usuario.getRol());
    }

    public String getUsername() {
        return username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getRol() {
        return rol;
    }

    public String getRolDescripcion() {
        return rolDescripcion;
    }

    public List<String> getPermisos() {
        return permisos;
    }
}
