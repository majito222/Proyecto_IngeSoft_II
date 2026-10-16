package com.avimanager.domain.port.in;

/**
 * Comando de F-08.2: el worker reporta un dano de infraestructura en su galpon
 * asignado (el galpon sale de su asignacion, no del formulario).
 */
public class ReportarDanoCommand {

    private final String solicitanteUsername;
    private final String tipoDano;
    private final String descripcion;

    public ReportarDanoCommand(String solicitanteUsername, String tipoDano, String descripcion) {
        this.solicitanteUsername = solicitanteUsername;
        this.tipoDano = tipoDano;
        this.descripcion = descripcion;
    }

    public String getSolicitanteUsername() {
        return solicitanteUsername;
    }

    public String getTipoDano() {
        return tipoDano;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
