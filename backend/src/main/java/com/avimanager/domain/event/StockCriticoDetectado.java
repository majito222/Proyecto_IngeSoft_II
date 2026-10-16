package com.avimanager.domain.event;

import com.avimanager.domain.model.Insumo;

import java.time.LocalDateTime;

/**
 * RN-03: el stock de un insumo acaba de bajar del 50% de la capacidad de la
 * bodega. Lo escuchan los notificadores del Administrador y del Dueño
 * (patron Observer), para que gestionen la compra.
 */
public class StockCriticoDetectado implements EventoDominio {

    private final Insumo insumo;
    private final double stockAnterior;
    private final LocalDateTime ocurridoEn;

    public StockCriticoDetectado(Insumo insumo, double stockAnterior, LocalDateTime ocurridoEn) {
        this.insumo = insumo;
        this.stockAnterior = stockAnterior;
        this.ocurridoEn = ocurridoEn;
    }

    @Override
    public LocalDateTime ocurridoEn() {
        return ocurridoEn;
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public double getStockAnterior() {
        return stockAnterior;
    }
}
