package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.model.Insumo;

/** F-03.2: un insumo de la bodega con su % disponible e indicador de Stock Crítico. */
public class InsumoResponse {

    private final String id;
    private final String nombre;
    private final String tipo;
    private final String unidad;
    private final double stock;
    private final double capacidad;
    private final double porcentajeDisponible;
    private final boolean critico;

    private InsumoResponse(Insumo insumo) {
        this.id = insumo.getId();
        this.nombre = insumo.getNombre();
        this.tipo = insumo.getTipo().name();
        this.unidad = insumo.getUnidad();
        this.stock = Math.round(insumo.getStock() * 10) / 10.0;
        this.capacidad = insumo.getCapacidad();
        this.porcentajeDisponible = Math.round(insumo.porcentajeDisponible() * 10) / 10.0;
        this.critico = insumo.esCritico();
    }

    public static InsumoResponse desde(Insumo insumo) {
        return new InsumoResponse(insumo);
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getUnidad() {
        return unidad;
    }

    public double getStock() {
        return stock;
    }

    public double getCapacidad() {
        return capacidad;
    }

    public double getPorcentajeDisponible() {
        return porcentajeDisponible;
    }

    public boolean isCritico() {
        return critico;
    }
}
