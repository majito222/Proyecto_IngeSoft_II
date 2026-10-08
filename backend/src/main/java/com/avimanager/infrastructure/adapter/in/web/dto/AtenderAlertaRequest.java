package com.avimanager.infrastructure.adapter.in.web.dto;

public class AtenderAlertaRequest {

    private String diagnostico;
    private String tratamiento;
    private boolean liberarLote;

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public boolean isLiberarLote() {
        return liberarLote;
    }

    public void setLiberarLote(boolean liberarLote) {
        this.liberarLote = liberarLote;
    }
}
