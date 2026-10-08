package com.avimanager.domain.event;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Turno;

import java.time.LocalDateTime;

/**
 * F-02.5: el veterinario atendio la alerta sanitaria y el turno bloqueado quedo
 * CERRADO_CON_ALERTA. Lo escucha el notificador del Administrador.
 */
public class AlertaSanitariaAtendida implements EventoDominio {

    private final AlertaSanitaria alerta;
    private final Lote lote;
    private final Turno turno;

    public AlertaSanitariaAtendida(AlertaSanitaria alerta, Lote lote, Turno turno) {
        this.alerta = alerta;
        this.lote = lote;
        this.turno = turno;
    }

    @Override
    public LocalDateTime ocurridoEn() {
        return alerta.getFechaAtencion();
    }

    public AlertaSanitaria getAlerta() {
        return alerta;
    }

    public Lote getLote() {
        return lote;
    }

    public Turno getTurno() {
        return turno;
    }
}
