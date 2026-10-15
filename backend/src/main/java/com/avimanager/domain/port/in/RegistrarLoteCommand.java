package com.avimanager.domain.port.in;

public class RegistrarLoteCommand {

    private final String galponId;
    private final Integer poblacionInicial;
    private final Integer edadSemanas;

    public RegistrarLoteCommand(String galponId, Integer poblacionInicial, Integer edadSemanas) {
        this.galponId = galponId;
        this.poblacionInicial = poblacionInicial;
        this.edadSemanas = edadSemanas;
    }

    public String getGalponId() {
        return galponId;
    }

    public Integer getPoblacionInicial() {
        return poblacionInicial;
    }

    public Integer getEdadSemanas() {
        return edadSemanas;
    }
}
