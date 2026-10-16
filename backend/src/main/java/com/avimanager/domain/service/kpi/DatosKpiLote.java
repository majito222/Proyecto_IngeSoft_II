package com.avimanager.domain.service.kpi;

import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.model.ReporteDiario;

import java.time.LocalDate;
import java.util.List;

/** Lo que necesita cada CalculadoraKpi: el lote, la fecha, sus reportes de turno y sus pesajes. */
public class DatosKpiLote {

    private final Lote lote;
    private final LocalDate fecha;
    private final List<ReporteDiario> reportes;
    private final List<Pesaje> pesajes;

    public DatosKpiLote(Lote lote, LocalDate fecha, List<ReporteDiario> reportes, List<Pesaje> pesajes) {
        this.lote = lote;
        this.fecha = fecha;
        this.reportes = List.copyOf(reportes);
        this.pesajes = List.copyOf(pesajes);
    }

    public Lote getLote() {
        return lote;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public List<ReporteDiario> getReportes() {
        return reportes;
    }

    public List<Pesaje> getPesajes() {
        return pesajes;
    }
}
