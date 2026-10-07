package com.avimanager.domain.event;

import java.time.LocalDateTime;

/**
 * Algo relevante que paso en el negocio y que otras partes del sistema pueden
 * querer saber (patron OBSERVER). El dominio solo publica eventos por medio de
 * PublicadorEventosPort; no sabe quien los escucha ni como se notifican.
 */
public interface EventoDominio {

    LocalDateTime ocurridoEn();
}
