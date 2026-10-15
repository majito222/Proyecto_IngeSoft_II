package com.avimanager.infrastructure.adapter.in.web.dto;

/** galponId null (o vacio) deja al worker sin galpon asignado. */
public class AsignarGalponRequest {

    private String galponId;

    public String getGalponId() {
        return galponId;
    }

    public void setGalponId(String galponId) {
        this.galponId = galponId;
    }
}
