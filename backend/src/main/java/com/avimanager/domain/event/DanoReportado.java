package com.avimanager.domain.event;

import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.OrdenMantenimiento;

import java.time.LocalDateTime;

/**
 * F-08.1: se reporto un dano de infraestructura, el galpon quedo
 * EN_RIESGO_SANITARIO y se creo una orden de mantenimiento. Lo escuchan los
 * notificadores del Tecnico y del Administrador.
 */
public class DanoReportado implements EventoDominio {

    private final OrdenMantenimiento orden;
    private final Galpon galpon;

    public DanoReportado(OrdenMantenimiento orden, Galpon galpon) {
        this.orden = orden;
        this.galpon = galpon;
    }

    @Override
    public LocalDateTime ocurridoEn() {
        return orden.getFechaReporte();
    }

    public OrdenMantenimiento getOrden() {
        return orden;
    }

    public Galpon getGalpon() {
        return galpon;
    }
}
