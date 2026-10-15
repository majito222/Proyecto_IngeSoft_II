package com.avimanager.domain.model;

/**
 * Tipos de dano de infraestructura que se reportan en F-08.
 */
public enum TipoDano {
    ROTURA_MALLA("Rotura de malla"),
    DANO_TECHO("Daño en techo"),
    CORTINA_DANADA("Cortina dañada"),
    OTRO("Otro daño");

    private final String descripcion;

    TipoDano(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
