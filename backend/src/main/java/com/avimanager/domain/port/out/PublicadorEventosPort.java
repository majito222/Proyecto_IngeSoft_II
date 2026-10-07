package com.avimanager.domain.port.out;

import com.avimanager.domain.event.EventoDominio;

/**
 * Puerto de salida del patron OBSERVER: el dominio publica un evento y la
 * infraestructura lo entrega a todos los suscriptores interesados (push/SMS,
 * correo, consola...). Agregar un nuevo destinatario no toca el dominio.
 */
public interface PublicadorEventosPort {

    void publicar(EventoDominio evento);
}
