package com.avimanager.domain.service;

import com.avimanager.domain.model.KpisLote;
import com.avimanager.domain.model.ReporteConsolidado;
import com.avimanager.domain.port.in.ConsultarKpisUseCase;
import com.avimanager.domain.port.out.ReporteConsolidadoRepositoryPort;
import com.avimanager.domain.service.kpi.CalculadorKpisLotes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** Implementa F-04.2: KPIs del dia de cada lote activo y el historial de reportes consolidados. */
public class ConsultarKpisService implements ConsultarKpisUseCase {

    private final CalculadorKpisLotes calculadorKpis;
    private final ReporteConsolidadoRepositoryPort reporteConsolidadoRepository;

    public ConsultarKpisService(CalculadorKpisLotes calculadorKpis,
                                ReporteConsolidadoRepositoryPort reporteConsolidadoRepository) {
        this.calculadorKpis = calculadorKpis;
        this.reporteConsolidadoRepository = reporteConsolidadoRepository;
    }

    @Override
    public List<KpisLote> calcularKpis(LocalDate fecha) {
        return calculadorKpis.calcular(fecha);
    }

    @Override
    public List<ReporteConsolidado> listarConsolidados() {
        return reporteConsolidadoRepository.listarTodos().stream()
                .sorted(Comparator.comparing(ReporteConsolidado::getFecha).reversed())
                .toList();
    }
}
