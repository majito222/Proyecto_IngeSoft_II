package com.avimanager.domain.port.in;

import com.avimanager.domain.model.ReporteConsolidado;

/**
 * F-04.1: el Administrador consolida el reporte de un dia con los KPIs de cada
 * lote. Se rechaza si queda algun turno de esa fecha sin aprobar (F-04.3).
 */
public interface ConsolidarReporteDiarioUseCase {

    ReporteConsolidado consolidar(ConsolidarReporteCommand comando);
}
