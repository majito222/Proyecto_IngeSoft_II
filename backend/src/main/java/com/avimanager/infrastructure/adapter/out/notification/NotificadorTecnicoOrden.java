package com.avimanager.infrastructure.adapter.out.notification;

import com.avimanager.domain.event.DanoReportado;
import com.avimanager.domain.model.OrdenMantenimiento;
import com.avimanager.infrastructure.adapter.out.event.SuscriptorEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observador de DanoReportado (F-08.1): simula el aviso al Tecnico de
 * mantenimiento con la orden asignada y su fecha limite (RN-04).
 */
@Component
public class NotificadorTecnicoOrden implements SuscriptorEvento<DanoReportado> {

    private static final Logger log = LoggerFactory.getLogger(NotificadorTecnicoOrden.class);

    @Override
    public Class<DanoReportado> tipoDeEvento() {
        return DanoReportado.class;
    }

    @Override
    public void alRecibir(DanoReportado evento) {
        OrdenMantenimiento orden = evento.getOrden();
        log.warn("[NOTIFICACION PUSH -> TECNICO] Nueva orden {} (prioridad {}) en {}: {} - {}. Reparar antes de {}.",
                orden.getId(), orden.getPrioridad(), evento.getGalpon().getNombre(),
                orden.getTipoDano().getDescripcion(), orden.getDescripcion(), orden.getFechaLimite());
    }
}
