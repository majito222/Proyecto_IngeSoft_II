package com.avimanager.infrastructure.adapter.out.event;

import com.avimanager.domain.event.EventoDominio;

/**
 * Observador del patron OBSERVER. Cada suscriptor declara el tipo de evento
 * que le interesa; PublicadorEventosEnMemoria solo le entrega esos eventos.
 * Para notificar a alguien nuevo basta con crear otro @Component que implemente
 * esta interfaz.
 */
public interface SuscriptorEvento<E extends EventoDominio> {

    Class<E> tipoDeEvento();

    void alRecibir(E evento);
}
