package com.avimanager.domain.model;

/**
 * RN-06: diferencia entre el stock que registra el sistema y el contado
 * fisicamente en la bodega para un insumo. Positiva si sobra, negativa si falta.
 */
public class DiferenciaInventario {

    private final String insumoId;
    private final String insumoNombre;
    private final String unidad;
    private final double stockSistema;
    private final double stockContado;

    public DiferenciaInventario(Insumo insumo, double stockContado) {
        this.insumoId = insumo.getId();
        this.insumoNombre = insumo.getNombre();
        this.unidad = insumo.getUnidad();
        this.stockSistema = insumo.getStock();
        this.stockContado = stockContado;
    }

    public double getDiferencia() {
        return stockContado - stockSistema;
    }

    public String getInsumoId() {
        return insumoId;
    }

    public String getInsumoNombre() {
        return insumoNombre;
    }

    public String getUnidad() {
        return unidad;
    }

    public double getStockSistema() {
        return stockSistema;
    }

    public double getStockContado() {
        return stockContado;
    }
}
