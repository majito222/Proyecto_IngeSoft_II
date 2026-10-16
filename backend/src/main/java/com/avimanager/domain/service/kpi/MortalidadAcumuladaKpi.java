package com.avimanager.domain.service.kpi;

import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.ResultadoKpi;

/**
 * Estrategia de KPI (patron Strategy): % de mortalidad acumulada del lote =
 * (poblacion inicial - aves vivas) / poblacion inicial x 100.
 */
public class MortalidadAcumuladaKpi implements CalculadoraKpi {

    public static final String CLAVE = "MORTALIDAD_ACUMULADA";

    @Override
    public ResultadoKpi calcular(DatosKpiLote datos) {
        Lote lote = datos.getLote();
        int muertas = lote.getPoblacionInicial() - lote.getPoblacionActual();
        double porcentaje = CalculadoraKpi.redondear(muertas * 100.0 / lote.getPoblacionInicial());
        return new ResultadoKpi(CLAVE, "Mortalidad acumulada", "%", porcentaje, false,
                muertas + " de " + lote.getPoblacionInicial() + " aves");
    }
}
