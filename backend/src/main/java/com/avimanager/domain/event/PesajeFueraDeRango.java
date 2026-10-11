package com.avimanager.domain.event;

import com.avimanager.domain.model.Pesaje;

import java.time.LocalDateTime;

/**
 * RN-02: el peso promedio de un lote se desvio mas de +-5% del estandar y el
 * pesaje quedo pendiente de revision. Lo escucha el notificador del Zootecnista.
 */
public class PesajeFueraDeRango implements EventoDominio {

    private final Pesaje pesaje;

    public PesajeFueraDeRango(Pesaje pesaje) {
        this.pesaje = pesaje;
    }

    @Override
    public LocalDateTime ocurridoEn() {
        return pesaje.getFechaHoraRegistro();
    }

    public Pesaje getPesaje() {
        return pesaje;
    }
}
