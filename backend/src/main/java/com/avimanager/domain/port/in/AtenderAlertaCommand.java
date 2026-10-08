package com.avimanager.domain.port.in;

/**
 * Comando de F-02.5: el veterinario atiende una alerta sanitaria. Si
 * liberarLote es true, el lote sale de observacion.
 */
public class AtenderAlertaCommand {

    private final String alertaId;
    private final String veterinarioUsername;
    private final String diagnostico;
    private final String tratamiento;
    private final boolean liberarLote;

    public AtenderAlertaCommand(String alertaId, String veterinarioUsername, String diagnostico,
                                String tratamiento, boolean liberarLote) {
        this.alertaId = alertaId;
        this.veterinarioUsername = veterinarioUsername;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.liberarLote = liberarLote;
    }

    public String getAlertaId() {
        return alertaId;
    }

    public String getVeterinarioUsername() {
        return veterinarioUsername;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public boolean isLiberarLote() {
        return liberarLote;
    }
}
