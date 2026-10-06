package com.avimanager.domain.port.in;

import com.avimanager.domain.model.Turno;

/**
 * Turno con los nombres que necesita mostrar el dashboard (responsable y galpon).
 */
public class ResumenTurno {

    private final Turno turno;
    private final String responsableNombre;
    private final String galponNombre;

    public ResumenTurno(Turno turno, String responsableNombre, String galponNombre) {
        this.turno = turno;
        this.responsableNombre = responsableNombre;
        this.galponNombre = galponNombre;
    }

    public Turno getTurno() {
        return turno;
    }

    public String getResponsableNombre() {
        return responsableNombre;
    }

    public String getGalponNombre() {
        return galponNombre;
    }
}
