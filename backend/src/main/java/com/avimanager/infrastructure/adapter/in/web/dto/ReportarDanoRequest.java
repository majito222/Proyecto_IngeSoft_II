package com.avimanager.infrastructure.adapter.in.web.dto;

public class ReportarDanoRequest {

    private String galponId;
    private String tipoDano;
    private String descripcion;

    public String getGalponId() {
        return galponId;
    }

    public void setGalponId(String galponId) {
        this.galponId = galponId;
    }

    public String getTipoDano() {
        return tipoDano;
    }

    public void setTipoDano(String tipoDano) {
        this.tipoDano = tipoDano;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
