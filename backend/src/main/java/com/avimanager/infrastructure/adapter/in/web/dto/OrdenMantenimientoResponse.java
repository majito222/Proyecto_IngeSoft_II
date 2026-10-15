package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.model.OrdenMantenimiento;
import com.avimanager.domain.port.in.ResumenOrden;

import java.time.LocalDateTime;

/**
 * Orden de mantenimiento para "Reportar daño" y "Órdenes de mantenimiento" (F-08).
 */
public class OrdenMantenimientoResponse {

    private final String id;
    private final String galponId;
    private final String galponNombre;
    private final String estadoGalpon;
    private final String tipoDano;
    private final String tipoDanoDescripcion;
    private final String descripcion;
    private final String prioridad;
    private final String estado;
    private final String reportadoPorNombre;
    private final LocalDateTime fechaReporte;
    private final LocalDateTime fechaLimite;
    private final boolean vencida;
    private final long minutosRestantes;
    private final String tecnicoNombre;
    private final LocalDateTime fechaInicioReparacion;
    private final String notaReparacion;
    private final LocalDateTime fechaCertificacion;

    private OrdenMantenimientoResponse(ResumenOrden resumen) {
        OrdenMantenimiento orden = resumen.getOrden();
        this.id = orden.getId();
        this.galponId = orden.getGalponId();
        this.galponNombre = resumen.getGalponNombre();
        this.estadoGalpon = resumen.getEstadoGalpon() == null ? null : resumen.getEstadoGalpon().name();
        this.tipoDano = orden.getTipoDano().name();
        this.tipoDanoDescripcion = orden.getTipoDano().getDescripcion();
        this.descripcion = orden.getDescripcion();
        this.prioridad = orden.getPrioridad().name();
        this.estado = orden.getEstado().name();
        this.reportadoPorNombre = resumen.getReportadoPorNombre();
        this.fechaReporte = orden.getFechaReporte();
        this.fechaLimite = orden.getFechaLimite();
        this.vencida = resumen.isVencida();
        this.minutosRestantes = resumen.getMinutosRestantes();
        this.tecnicoNombre = resumen.getTecnicoNombre();
        this.fechaInicioReparacion = orden.getFechaInicioReparacion();
        this.notaReparacion = orden.getNotaReparacion();
        this.fechaCertificacion = orden.getFechaCertificacion();
    }

    public static OrdenMantenimientoResponse desde(ResumenOrden resumen) {
        return new OrdenMantenimientoResponse(resumen);
    }

    public String getId() {
        return id;
    }

    public String getGalponId() {
        return galponId;
    }

    public String getGalponNombre() {
        return galponNombre;
    }

    public String getEstadoGalpon() {
        return estadoGalpon;
    }

    public String getTipoDano() {
        return tipoDano;
    }

    public String getTipoDanoDescripcion() {
        return tipoDanoDescripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public String getEstado() {
        return estado;
    }

    public String getReportadoPorNombre() {
        return reportadoPorNombre;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public LocalDateTime getFechaLimite() {
        return fechaLimite;
    }

    public boolean isVencida() {
        return vencida;
    }

    public long getMinutosRestantes() {
        return minutosRestantes;
    }

    public String getTecnicoNombre() {
        return tecnicoNombre;
    }

    public LocalDateTime getFechaInicioReparacion() {
        return fechaInicioReparacion;
    }

    public String getNotaReparacion() {
        return notaReparacion;
    }

    public LocalDateTime getFechaCertificacion() {
        return fechaCertificacion;
    }
}
