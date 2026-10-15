package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.model.Usuario;

/** Worker (operario) y el galpon que tiene asignado (F-11). */
public class WorkerResponse {

    private final String username;
    private final String nombreCompleto;
    private final String galponAsignadoId;

    private WorkerResponse(Usuario usuario) {
        this.username = usuario.getUsername();
        this.nombreCompleto = usuario.getNombreCompleto();
        this.galponAsignadoId = usuario.getGalponAsignadoId();
    }

    public static WorkerResponse desde(Usuario usuario) {
        return new WorkerResponse(usuario);
    }

    public String getUsername() {
        return username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getGalponAsignadoId() {
        return galponAsignadoId;
    }
}
