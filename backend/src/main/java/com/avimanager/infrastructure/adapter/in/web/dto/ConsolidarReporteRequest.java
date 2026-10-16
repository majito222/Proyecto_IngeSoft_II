package com.avimanager.infrastructure.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.Map;

public class ConsolidarReporteRequest {

    private LocalDate fecha;
    private Map<String, Double> conteoFisico;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Map<String, Double> getConteoFisico() {
        return conteoFisico;
    }

    public void setConteoFisico(Map<String, Double> conteoFisico) {
        this.conteoFisico = conteoFisico;
    }
}
