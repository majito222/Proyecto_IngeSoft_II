package com.avimanager.domain.port.in;

/**
 * Comando de F-07.1: el worker registra el peso promedio de la muestra de su
 * galpon asignado. Los valores llegan como Double/Integer para poder validar
 * que no falten.
 */
public class RegistrarPesajeCommand {

    private final String workerUsername;
    private final Double pesoPromedioGramos;
    private final Integer tamanoMuestra;

    public RegistrarPesajeCommand(String workerUsername, Double pesoPromedioGramos, Integer tamanoMuestra) {
        this.workerUsername = workerUsername;
        this.pesoPromedioGramos = pesoPromedioGramos;
        this.tamanoMuestra = tamanoMuestra;
    }

    public String getWorkerUsername() {
        return workerUsername;
    }

    public Double getPesoPromedioGramos() {
        return pesoPromedioGramos;
    }

    public Integer getTamanoMuestra() {
        return tamanoMuestra;
    }
}
