package com.avimanager.domain.port.in;

import java.time.LocalDate;

public class ConsolidarReporteCommand {

    private final LocalDate fecha;
    private final String administradorUsername;

    public ConsolidarReporteCommand(LocalDate fecha, String administradorUsername) {
        this.fecha = fecha;
        this.administradorUsername = administradorUsername;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getAdministradorUsername() {
        return administradorUsername;
    }
}
