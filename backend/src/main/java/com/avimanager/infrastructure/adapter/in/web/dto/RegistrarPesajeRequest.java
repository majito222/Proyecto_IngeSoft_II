package com.avimanager.infrastructure.adapter.in.web.dto;

public class RegistrarPesajeRequest {

    private Double pesoPromedioGramos;
    private Integer tamanoMuestra;

    public Double getPesoPromedioGramos() {
        return pesoPromedioGramos;
    }

    public void setPesoPromedioGramos(Double pesoPromedioGramos) {
        this.pesoPromedioGramos = pesoPromedioGramos;
    }

    public Integer getTamanoMuestra() {
        return tamanoMuestra;
    }

    public void setTamanoMuestra(Integer tamanoMuestra) {
        this.tamanoMuestra = tamanoMuestra;
    }
}
