package com.avimanager.domain.port.in;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.Turno;

/**
 * Alerta sanitaria con los nombres que muestra la pantalla: galpon, responsable
 * del turno bloqueado y quien la atendio.
 */
public class ResumenAlerta {

    private final AlertaSanitaria alerta;
    private final Turno turno;
    private final String galponNombre;
    private final String responsableTurnoNombre;
    private final String atendidaPorNombre;

    public ResumenAlerta(AlertaSanitaria alerta, Turno turno, String galponNombre,
                         String responsableTurnoNombre, String atendidaPorNombre) {
        this.alerta = alerta;
        this.turno = turno;
        this.galponNombre = galponNombre;
        this.responsableTurnoNombre = responsableTurnoNombre;
        this.atendidaPorNombre = atendidaPorNombre;
    }

    public AlertaSanitaria getAlerta() {
        return alerta;
    }

    /** Puede ser null si la alerta no quedo asociada a un turno. */
    public Turno getTurno() {
        return turno;
    }

    public String getGalponNombre() {
        return galponNombre;
    }

    public String getResponsableTurnoNombre() {
        return responsableTurnoNombre;
    }

    public String getAtendidaPorNombre() {
        return atendidaPorNombre;
    }
}
