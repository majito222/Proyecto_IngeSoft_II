package com.avimanager.domain.port.in;

public class IniciarTurnoCommand {

    private final String workerUsername;

    public IniciarTurnoCommand(String workerUsername) {
        this.workerUsername = workerUsername;
    }

    public String getWorkerUsername() {
        return workerUsername;
    }
}
