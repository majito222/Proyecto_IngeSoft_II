package com.avimanager.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * F-04.1: el reporte consolidado de un dia, con los KPIs de cada lote, que el
 * Administrador genera cuando todos los turnos de esa fecha estan aprobados y el
 * inventario fisico cuadra con el sistema (RN-06). Guarda el conteo fisico como
 * evidencia. Es una foto: no cambia aunque despues cambien los datos.
 */
public class ReporteConsolidado {

    private final String id;
    private final LocalDate fecha;
    private final String consolidadoPor;
    private final String consolidadoPorNombre;
    private final LocalDateTime fechaHora;
    private final int turnosAprobados;
    private final List<KpisLote> lotes;
    private final Map<String, Double> conteoFisico;

    public ReporteConsolidado(String id, LocalDate fecha, String consolidadoPor, String consolidadoPorNombre,
                              LocalDateTime fechaHora, int turnosAprobados, List<KpisLote> lotes,
                              Map<String, Double> conteoFisico) {
        this.id = id;
        this.fecha = fecha;
        this.consolidadoPor = consolidadoPor;
        this.consolidadoPorNombre = consolidadoPorNombre;
        this.fechaHora = fechaHora;
        this.turnosAprobados = turnosAprobados;
        this.lotes = List.copyOf(lotes);
        this.conteoFisico = Map.copyOf(conteoFisico);
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

    /** Conteo fisico de cada insumo (insumoId -> cantidad) con el que se concilio el dia (RN-06). */
    public Map<String, Double> getConteoFisico() {
        return conteoFisico;
    }
}
