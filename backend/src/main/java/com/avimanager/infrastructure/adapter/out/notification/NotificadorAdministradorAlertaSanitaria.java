package com.avimanager.infrastructure.adapter.out.notification;

import com.avimanager.domain.event.AlertaSanitariaGenerada;
import com.avimanager.infrastructure.adapter.out.event.SuscriptorEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observador de AlertaSanitariaGenerada: RN-01 tambien exige avisar al
 * Administrador. Se agrego como un suscriptor mas, sin tocar CerrarTurnoService.
 */
@Component
public class NotificadorAdministradorAlertaSanitaria implements SuscriptorEvento<AlertaSanitariaGenerada> {

    private static final Logger log = LoggerFactory.getLogger(NotificadorAdministradorAlertaSanitaria.class);

    @Override
    public Class<AlertaSanitariaGenerada> tipoDeEvento() {
        return AlertaSanitariaGenerada.class;
    }

    @Override
    public void alRecibir(AlertaSanitariaGenerada evento) {
        log.warn("[NOTIFICACION PUSH/SMS -> ADMINISTRADOR] Alerta sanitaria en el lote {} (galpon {}): {}% de mortalidad. "
                        + "El cierre del turno quedo bloqueado hasta que el veterinario atienda la alerta.",
                evento.getLote().getId(), evento.getLote().getGalponId(),
                String.format("%.2f", evento.getAlerta().getPorcentajeMortalidad()));
    }
}
