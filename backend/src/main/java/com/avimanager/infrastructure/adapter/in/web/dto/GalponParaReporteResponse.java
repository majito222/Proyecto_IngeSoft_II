package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.port.in.GalponParaReporte;

import java.util.List;

/**
 * Galpon que aparece en "Reportar daño" (F-08.2), con sus ordenes pendientes.
 */
public class GalponParaReporteResponse {

    private final String galponId;
    private final String galponNombre;
    private final String estado;
    private final List<OrdenMantenimientoResponse> ordenesPendientes;

    private GalponParaReporteResponse(GalponParaReporte galpon) {
        this.galponId = galpon.getGalponId();
        this.galponNombre = galpon.getGalponNombre();
        this.estado = galpon.getEstado().name();
        this.ordenesPendientes = galpon.getOrdenesPendientes().stream().map(OrdenMantenimientoResponse::desde).toList();
    }

    public static GalponParaReporteResponse desde(GalponParaReporte galpon) {
        return new GalponParaReporteResponse(galpon);
    }

    public String getGalponId() {
        return galponId;
    }

    public String getGalponNombre() {
        return galponNombre;
    }

    public String getEstado() {
        return estado;
    }

    public List<OrdenMantenimientoResponse> getOrdenesPendientes() {
        return ordenesPendientes;
    }
}
