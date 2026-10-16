package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.model.ReporteConsolidado;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** F-04.1: un reporte diario consolidado. */
public class ReporteConsolidadoResponse {

    private final String id;
    private final LocalDate fecha;
    private final String consolidadoPor;
    private final String consolidadoPorNombre;
    private final LocalDateTime fechaHora;
    private final int turnosAprobados;
    private final List<KpisLoteResponse> lotes;

    private ReporteConsolidadoResponse(ReporteConsolidado reporte) {
        this.id = reporte.getId();
        this.fecha = reporte.getFecha();
        this.consolidadoPor = reporte.getConsolidadoPor();
        this.consolidadoPorNombre = reporte.getConsolidadoPorNombre();
        this.fechaHora = reporte.getFechaHora();
        this.turnosAprobados = reporte.getTurnosAprobados();
        this.lotes = reporte.getLotes().stream().map(KpisLoteResponse::desde).toList();
    }

    public static ReporteConsolidadoResponse desde(ReporteConsolidado reporte) {
        return new ReporteConsolidadoResponse(reporte);
    }

    public String getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getConsolidadoPor() {
        return consolidadoPor;
    }

    public String getConsolidadoPorNombre() {
        return consolidadoPorNombre;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public int getTurnosAprobados() {
        return turnosAprobados;
    }

    public List<KpisLoteResponse> getLotes() {
        return lotes;
    }
}
