package com.avimanager.infrastructure.adapter.out.notification;

import com.avimanager.domain.event.AlertaSanitariaAtendida;
import com.avimanager.infrastructure.adapter.out.event.SuscriptorEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observador de AlertaSanitariaAtendida (F-02.5): avisa al Administrador que el
 * veterinario atendio la alerta y que el turno quedo cerrado con alerta, listo
 * para su aprobacion.
 */
@Component
public class NotificadorAdministradorAlertaAtendida implements SuscriptorEvento<AlertaSanitariaAtendida> {

    private static final Logger log = LoggerFactory.getLogger(NotificadorAdministradorAlertaAtendida.class);

    @Override
    public Class<AlertaSanitariaAtendida> tipoDeEvento() {
        return AlertaSanitariaAtendida.class;
    }

    @Override
    public void alRecibir(AlertaSanitariaAtendida evento) {
        log.info("[NOTIFICACION -> ADMINISTRADOR] El veterinario {} atendio la alerta del lote {}. Diagnostico: {}. "
                        + "El turno de {} quedo {} y el lote esta {}.",
                evento.getAlerta().getAtendidaPor(), evento.getLote().getId(), evento.getAlerta().getDiagnostico(),
                evento.getTurno().getWorkerUsername(), evento.getTurno().getEstado(), evento.getLote().getEstado());
    }
}
