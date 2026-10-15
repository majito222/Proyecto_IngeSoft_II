package com.avimanager.domain.port.in;

public class AsignarWorkerCommand {

    private final String workerUsername;
    private final String galponId;

    /** galponId null deja al worker sin galpon asignado. */
    public AsignarWorkerCommand(String workerUsername, String galponId) {
        this.workerUsername = workerUsername;
        this.galponId = galponId;
    }

    public String getWorkerUsername() {
        return workerUsername;
    }

    public String getGalponId() {
        return galponId;
    }
}
