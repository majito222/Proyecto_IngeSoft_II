package com.avimanager.infrastructure.adapter.out.notification;

import com.avimanager.domain.event.AlertaSanitariaGenerada;
import com.avimanager.infrastructure.adapter.out.event.SuscriptorEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observador de AlertaSanitariaGenerada (RN-01): simula el push/SMS al
 * Veterinario. Puede reemplazarse por uno real (Twilio, Firebase...) sin cambiar
 * el dominio.
 */
@Component
public class NotificadorVeterinarioAlertaSanitaria implements SuscriptorEvento<AlertaSanitariaGenerada> {

    private static final Logger log = LoggerFactory.getLogger(NotificadorVeterinarioAlertaSanitaria.class);

    @Override
    public Class<AlertaSanitariaGenerada> tipoDeEvento() {
        return AlertaSanitariaGenerada.class;
    }

    @Override
    public void alRecibir(AlertaSanitariaGenerada evento) {
        log.warn("[NOTIFICACION PUSH/SMS -> VETERINARIO] Lote {} (galpon {}) supero el umbral de mortalidad: {}% a las {}. "
                        + "Lote puesto en estado {}.",
                evento.getLote().getId(), evento.getLote().getGalponId(),
                String.format("%.2f", evento.getAlerta().getPorcentajeMortalidad()),
                evento.getAlerta().getFechaHora(), evento.getLote().getEstado());
    }
}
