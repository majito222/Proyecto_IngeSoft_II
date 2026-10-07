package com.avimanager.domain.event;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.Lote;

import java.time.LocalDateTime;

/**
 * RN-01: la mortalidad diaria de un lote supero el umbral critico y se genero
 * una alerta sanitaria. Lo escuchan los notificadores del Veterinario y del
 * Administrador.
 */
public class AlertaSanitariaGenerada implements EventoDominio {

    private final AlertaSanitaria alerta;
    private final Lote lote;

    public AlertaSanitariaGenerada(AlertaSanitaria alerta, Lote lote) {
        this.alerta = alerta;
        this.lote = lote;
    }

    @Override
    public LocalDateTime ocurridoEn() {
        return alerta.getFechaHora();
    }

    public AlertaSanitaria getAlerta() {
        return alerta;
    }

    public Lote getLote() {
        return lote;
    }
}
