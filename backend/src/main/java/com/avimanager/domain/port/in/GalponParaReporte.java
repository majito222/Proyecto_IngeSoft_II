package com.avimanager.domain.port.in;

import com.avimanager.domain.model.EstadoGalpon;

import java.util.List;

/**
 * Galpon en el que el usuario puede reportar danos (F-08.2), con su estado y
 * las ordenes de mantenimiento que siguen pendientes.
 */
public class GalponParaReporte {

    private final String galponId;
    private final String galponNombre;
    private final EstadoGalpon estado;
    private final List<ResumenOrden> ordenesPendientes;

    public GalponParaReporte(String galponId, String galponNombre, EstadoGalpon estado,
                             List<ResumenOrden> ordenesPendientes) {
        this.galponId = galponId;
        this.galponNombre = galponNombre;
        this.estado = estado;
        this.ordenesPendientes = ordenesPendientes;
    }

    public String getGalponId() {
        return galponId;
    }

    public String getGalponNombre() {
        return galponNombre;
    }

    public EstadoGalpon getEstado() {
        return estado;
    }

    public List<ResumenOrden> getOrdenesPendientes() {
        return ordenesPendientes;
    }
}
