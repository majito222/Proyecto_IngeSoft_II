package com.avimanager.infrastructure.adapter.in.web.dto;

public class ReportarDanoRequest {

    private String tipoDano;
    private String descripcion;

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
