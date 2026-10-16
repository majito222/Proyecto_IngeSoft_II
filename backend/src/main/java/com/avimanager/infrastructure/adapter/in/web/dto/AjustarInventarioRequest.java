package com.avimanager.infrastructure.adapter.in.web.dto;

public class AjustarInventarioRequest {

    private Double stockContado;
    private String justificacion;

    public Double getStockContado() {
        return stockContado;
    }

    public void setStockContado(Double stockContado) {
        this.stockContado = stockContado;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }
}
