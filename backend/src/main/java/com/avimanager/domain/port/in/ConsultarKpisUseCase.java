package com.avimanager.domain.port.in;

import com.avimanager.domain.model.KpisLote;
import com.avimanager.domain.model.ReporteConsolidado;

import java.time.LocalDate;
import java.util.List;

/** F-04.2: KPIs de todos los lotes activos en una fecha, y los reportes ya consolidados. */
public interface ConsultarKpisUseCase {

    List<KpisLote> calcularKpis(LocalDate fecha);

    List<ReporteConsolidado> listarConsolidados();
}
