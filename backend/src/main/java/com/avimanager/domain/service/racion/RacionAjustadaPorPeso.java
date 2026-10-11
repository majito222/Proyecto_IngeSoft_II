package com.avimanager.domain.service.racion;

import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Pesaje;

import java.util.Locale;

/**
 * Estrategia de racion ajustada por peso (F-07, RN-02): parte de la estrategia
 * base y la corrige en proporcion al peso. Si las aves pesan menos que el
 * estandar comen mas, y si pesan mas comen menos:
 *
 *   gramos = gramos de la base x (peso estandar / peso real)
 *
 * El ajuste se limita a +-15% para que un pesaje mal tomado no dispare la racion.
 * Solo se usa cuando el Zootecnista aprobo el ajuste (ver SelectorEstrategiaRacion).
 */
public class RacionAjustadaPorPeso implements EstrategiaRacion {

    public static final double AJUSTE_MAXIMO = 0.15;

    private final EstrategiaRacion base;
    private final double factor;

    public RacionAjustadaPorPeso(EstrategiaRacion base, Pesaje pesaje) {
        this.base = base;
        double factorSinLimite = pesaje.getPesoEstandarGramos() / pesaje.getPesoPromedioGramos();
        this.factor = Math.max(1 - AJUSTE_MAXIMO, Math.min(1 + AJUSTE_MAXIMO, factorSinLimite));
    }

    @Override
    public double gramosPorAve(Lote lote) {
        return Math.round(base.gramosPorAve(lote) * factor * 10.0) / 10.0;
    }

    @Override
    public String nombre() {
        return String.format(Locale.ROOT, "Ajustada por peso (%+.1f%%)", (factor - 1) * 100);
    }
}
