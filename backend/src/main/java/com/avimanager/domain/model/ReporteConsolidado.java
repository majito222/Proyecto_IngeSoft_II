package com.avimanager.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * F-04.1: el reporte consolidado de un dia, con los KPIs de cada lote, que el
 * Administrador genera cuando todos los turnos de esa fecha estan aprobados.
 * Es una foto: no cambia aunque despues cambien los datos.
 */
public class ReporteConsolidado {

    private final String id;
    private final LocalDate fecha;
    private final String consolidadoPor;
    private final String consolidadoPorNombre;
    private final LocalDateTime fechaHora;
    private final int turnosAprobados;
    private final List<KpisLote> lotes;

    public ReporteConsolidado(String id, LocalDate fecha, String consolidadoPor, String consolidadoPorNombre,
                              LocalDateTime fechaHora, int turnosAprobados, List<KpisLote> lotes) {
        this.id = id;
        this.fecha = fecha;
        this.consolidadoPor = consolidadoPor;
        this.consolidadoPorNombre = consolidadoPorNombre;
        this.fechaHora = fechaHora;
        this.turnosAprobados = turnosAprobados;
        this.lotes = List.copyOf(lotes);
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

    public List<KpisLote> getLotes() {
        return lotes;
    }
}
