package com.avimanager.domain.service.racion;

import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.port.out.PesajeRepositoryPort;

import java.util.Optional;

/**
 * Elige la estrategia de racion para cada lote (contexto del patron Strategy):
 *   - Si el ultimo pesaje del lote se desvio mas de 5% y el Zootecnista aprobo
 *     el ajuste (RN-02, F-07), usa la racion ajustada por peso.
 *   - En cualquier otro caso, la tabla estandar (F-06).
 * Como se mira el ultimo pesaje, un pesaje nuevo dentro de rango devuelve el
 * lote a la tabla estandar.
 */
public class SelectorEstrategiaRacion {

    private final EstrategiaRacion tablaEstandar;
    private final PesajeRepositoryPort pesajeRepository;

    public SelectorEstrategiaRacion(EstrategiaRacion tablaEstandar, PesajeRepositoryPort pesajeRepository) {
        this.tablaEstandar = tablaEstandar;
        this.pesajeRepository = pesajeRepository;
    }

    public EstrategiaRacion para(Lote lote) {
        Optional<Pesaje> ultimo = pesajeRepository.ultimoPesajeDelLote(lote.getId());
        if (ultimo.isPresent() && ultimo.get().tieneAjusteAprobado()) {
            return new RacionAjustadaPorPeso(tablaEstandar, ultimo.get());
        }
        return tablaEstandar;
    }
}
