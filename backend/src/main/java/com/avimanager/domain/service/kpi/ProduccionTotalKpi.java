package com.avimanager.domain.service.kpi;

import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.model.ResultadoKpi;

/**
 * Estrategia de KPI (patron Strategy): produccion total del lote en el dia, la
 * suma de las bandejas de huevo reportadas en los cierres de turno de esa fecha.
 */
public class ProduccionTotalKpi implements CalculadoraKpi {

    public static final String CLAVE = "PRODUCCION_TOTAL";

    @Override
    public ResultadoKpi calcular(DatosKpiLote datos) {
        int bandejas = datos.getReportes().stream()
                .filter(r -> r.getFecha().equals(datos.getFecha()))
                .filter(r -> r.getProduccionHuevosBandejas() != null)
                .mapToInt(ReporteDiario::getProduccionHuevosBandejas)
                .sum();
        long reportes = datos.getReportes().stream().filter(r -> r.getFecha().equals(datos.getFecha())).count();
        return new ResultadoKpi(CLAVE, "Producción total", "bandejas", (double) bandejas, false,
                reportes == 0 ? "Sin cierres de turno en la fecha" : null);
    }
}
