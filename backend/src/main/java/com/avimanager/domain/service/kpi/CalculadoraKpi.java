package com.avimanager.domain.service.kpi;

import com.avimanager.domain.model.ResultadoKpi;

/**
 * Patron STRATEGY (F-04): cada KPI del reporte diario es una estrategia de
 * calculo intercambiable. La consolidacion recorre la lista de calculadoras sin
 * saber cual es cual, asi que agregar un KPI nuevo es agregar una clase y
 * registrarla en UseCaseConfig, sin tocar el servicio.
 */
public interface CalculadoraKpi {

    ResultadoKpi calcular(DatosKpiLote datos);

    static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
