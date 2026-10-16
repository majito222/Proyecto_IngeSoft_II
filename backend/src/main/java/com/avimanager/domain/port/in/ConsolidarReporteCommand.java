package com.avimanager.domain.port.in;

import java.time.LocalDate;
import java.util.Map;

/**
 * Comando de F-04.1: el Administrador consolida el dia. Trae el conteo fisico de
 * cada insumo de la bodega (insumoId -> cantidad contada) para validar RN-06.
 */
public class ConsolidarReporteCommand {

    private final LocalDate fecha;
    private final String administradorUsername;
    private final Map<String, Double> conteoFisico;

    public ConsolidarReporteCommand(LocalDate fecha, String administradorUsername, Map<String, Double> conteoFisico) {
        this.fecha = fecha;
        this.administradorUsername = administradorUsername;
        this.conteoFisico = conteoFisico == null ? Map.of() : Map.copyOf(conteoFisico);
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getAdministradorUsername() {
        return administradorUsername;
    }

    public Map<String, Double> getConteoFisico() {
        return conteoFisico;
    }
}
