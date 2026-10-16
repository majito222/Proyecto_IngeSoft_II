package com.avimanager.domain.port.out;

import com.avimanager.domain.model.ReporteConsolidado;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Reportes diarios consolidados (F-04.1), uno por fecha. */
public interface ReporteConsolidadoRepositoryPort {

    ReporteConsolidado guardar(ReporteConsolidado reporte);

    Optional<ReporteConsolidado> buscarPorFecha(LocalDate fecha);

    List<ReporteConsolidado> listarTodos();
}
