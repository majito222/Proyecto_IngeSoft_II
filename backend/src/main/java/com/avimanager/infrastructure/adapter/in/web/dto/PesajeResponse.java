package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.port.in.ResumenPesaje;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Pesaje semanal para las paginas "Pesaje semanal" y "Revision de pesajes" (F-07).
 */
public class PesajeResponse {

    private final String id;
    private final String loteId;
    private final String galponId;
    private final String galponNombre;
    private final LocalDate fecha;
    private final LocalDateTime fechaHoraRegistro;
    private final int edadSemanas;
    private final double pesoPromedioGramos;
    private final int tamanoMuestra;
    private final double pesoEstandarGramos;
    private final double desviacionPorcentaje;
    private final boolean fueraDeRango;
    private final String estado;
    private final String registradoPor;
    private final String registradoPorNombre;
    private final String revisadoPorNombre;
    private final LocalDateTime fechaRevision;

    private PesajeResponse(ResumenPesaje resumen) {
        Pesaje pesaje = resumen.getPesaje();
        this.id = pesaje.getId();
        this.loteId = pesaje.getLoteId();
        this.galponId = pesaje.getGalponId();
        this.galponNombre = resumen.getGalponNombre();
        this.fecha = pesaje.getFecha();
        this.fechaHoraRegistro = pesaje.getFechaHoraRegistro();
        this.edadSemanas = pesaje.getEdadSemanas();
        this.pesoPromedioGramos = pesaje.getPesoPromedioGramos();
        this.tamanoMuestra = pesaje.getTamanoMuestra();
        this.pesoEstandarGramos = pesaje.getPesoEstandarGramos();
        this.desviacionPorcentaje = pesaje.getDesviacionPorcentaje();
        this.fueraDeRango = pesaje.estaFueraDeRango();
        this.estado = pesaje.getEstado().name();
        this.registradoPor = pesaje.getRegistradoPor();
        this.registradoPorNombre = resumen.getRegistradoPorNombre();
        this.revisadoPorNombre = resumen.getRevisadoPorNombre();
        this.fechaRevision = pesaje.getFechaRevision();
    }

    public static PesajeResponse desde(ResumenPesaje resumen) {
        return resumen == null ? null : new PesajeResponse(resumen);
    }

    public String getId() {
        return id;
    }

    public String getLoteId() {
        return loteId;
    }

    public String getGalponId() {
        return galponId;
    }

    public String getGalponNombre() {
        return galponNombre;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalDateTime getFechaHoraRegistro() {
        return fechaHoraRegistro;
    }

    public int getEdadSemanas() {
        return edadSemanas;
    }

    public double getPesoPromedioGramos() {
        return pesoPromedioGramos;
    }

    public int getTamanoMuestra() {
        return tamanoMuestra;
    }

    public double getPesoEstandarGramos() {
        return pesoEstandarGramos;
    }

    public double getDesviacionPorcentaje() {
        return desviacionPorcentaje;
    }

    public boolean isFueraDeRango() {
        return fueraDeRango;
    }

    public String getEstado() {
        return estado;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public String getRegistradoPorNombre() {
        return registradoPorNombre;
    }

    public String getRevisadoPorNombre() {
        return revisadoPorNombre;
    }

    public LocalDateTime getFechaRevision() {
        return fechaRevision;
    }
}
