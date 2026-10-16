package com.avimanager.domain.port.in;

public class AprobarReporteCommand {

    private final String turnoId;
    private final String administradorUsername;

    public AprobarReporteCommand(String turnoId, String administradorUsername) {
        this.turnoId = turnoId;
        this.administradorUsername = administradorUsername;
    }

    public String getTurnoId() {
        return turnoId;
    }

    public String getAdministradorUsername() {
        return administradorUsername;
    }
}
