package com.avimanager.infrastructure.adapter.out.notification;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.port.out.NotificadorAlertaSanitariaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida que simula el envio de notificacion push/SMS al
 * Veterinario (RN-01). En una entrega posterior este adaptador puede
 * reemplazarse por uno real (Twilio, Firebase, etc.) sin cambiar el dominio.
 */
@Component
public class ConsoleNotificadorAlertaSanitariaAdapter implements NotificadorAlertaSanitariaPort {

    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificadorAlertaSanitariaAdapter.class);

    @Override
    public void notificarAlVeterinario(AlertaSanitaria alerta, Lote lote) {
        log.warn("[NOTIFICACION PUSH/SMS -> VETERINARIO] Lote {} (galpon {}) supero el umbral de mortalidad: {}% a las {}. "
                        + "Lote puesto en estado {}.",
                lote.getId(), lote.getGalponId(), String.format("%.2f", alerta.getPorcentajeMortalidad()),
                alerta.getFechaHora(), lote.getEstado());
    }
}
