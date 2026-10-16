package com.avimanager.infrastructure.adapter.in.web.dto;

import java.time.LocalDate;

public class ConsolidarReporteRequest {

    private LocalDate fecha;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}
