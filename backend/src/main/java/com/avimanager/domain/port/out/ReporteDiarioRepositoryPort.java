package com.avimanager.domain.port.out;

import com.avimanager.domain.model.ReporteDiario;

import java.util.List;
import java.util.Optional;

public interface ReporteDiarioRepositoryPort {

    ReporteDiario guardar(ReporteDiario reporteDiario);

    Optional<ReporteDiario> buscarPorId(String reporteId);

    /** Reportes de turno del lote (F-04: consumo y produccion para los KPIs). */
    List<ReporteDiario> listarPorLote(String loteId);
}
