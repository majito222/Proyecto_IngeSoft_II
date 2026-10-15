package com.avimanager.infrastructure.adapter.in.web.dto;

public class RegistrarLoteRequest {

    private Integer poblacionInicial;
    private Integer edadSemanas;

    public Integer getPoblacionInicial() {
        return poblacionInicial;
    }

    public void setPoblacionInicial(Integer poblacionInicial) {
        this.poblacionInicial = poblacionInicial;
    }

    public Integer getEdadSemanas() {
        return edadSemanas;
    }

    public void setEdadSemanas(Integer edadSemanas) {
        this.edadSemanas = edadSemanas;
    }
}
