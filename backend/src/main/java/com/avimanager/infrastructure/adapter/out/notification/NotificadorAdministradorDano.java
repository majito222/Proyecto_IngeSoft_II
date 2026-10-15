package com.avimanager.infrastructure.adapter.out.notification;

import com.avimanager.domain.event.DanoReportado;
import com.avimanager.infrastructure.adapter.out.event.SuscriptorEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observador de DanoReportado (F-08.1): avisa al Administrador que un galpon
 * quedo en riesgo sanitario por un dano de infraestructura.
 */
@Component
public class NotificadorAdministradorDano implements SuscriptorEvento<DanoReportado> {

    private static final Logger log = LoggerFactory.getLogger(NotificadorAdministradorDano.class);

    @Override
    public Class<DanoReportado> tipoDeEvento() {
        return DanoReportado.class;
    }

    @Override
    public void alRecibir(DanoReportado evento) {
        log.warn("[NOTIFICACION -> ADMINISTRADOR] {} quedo en estado {} por {} reportado por {}.",
                evento.getGalpon().getNombre(), evento.getGalpon().getEstado(),
                evento.getOrden().getTipoDano().getDescripcion(), evento.getOrden().getReportadoPor());
    }
}
