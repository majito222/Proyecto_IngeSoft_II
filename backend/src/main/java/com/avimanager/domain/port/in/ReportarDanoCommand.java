package com.avimanager.domain.port.in;

/**
 * Comando de F-08.2: reportar un dano de infraestructura. Si galponId es null,
 * se usa el galpon asignado al worker.
 */
public class ReportarDanoCommand {

    private final String solicitanteUsername;
    private final String galponId;
    private final String tipoDano;
    private final String descripcion;

    public ReportarDanoCommand(String solicitanteUsername, String galponId, String tipoDano, String descripcion) {
        this.solicitanteUsername = solicitanteUsername;
        this.galponId = galponId;
        this.tipoDano = tipoDano;
        this.descripcion = descripcion;
    }

    public String getSolicitanteUsername() {
        return solicitanteUsername;
    }

    public String getGalponId() {
        return galponId;
    }

    public String getTipoDano() {
        return tipoDano;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
