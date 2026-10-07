package com.avimanager.infrastructure.adapter.out.event;

import com.avimanager.domain.event.EventoDominio;
import com.avimanager.domain.port.out.PublicadorEventosPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sujeto del patron OBSERVER: recibe cada evento del dominio y lo entrega, en el
 * mismo hilo, a todos los suscriptores de ese tipo de evento. Spring inyecta la
 * lista con todos los SuscriptorEvento registrados. Si un suscriptor falla, se
 * registra el error y los demas igual reciben el evento.
 */
@Component
public class PublicadorEventosEnMemoria implements PublicadorEventosPort {

    private static final Logger log = LoggerFactory.getLogger(PublicadorEventosEnMemoria.class);

    private final List<SuscriptorEvento<?>> suscriptores;

    public PublicadorEventosEnMemoria(List<SuscriptorEvento<?>> suscriptores) {
        this.suscriptores = List.copyOf(suscriptores);
    }

    @Override
    public void publicar(EventoDominio evento) {
        for (SuscriptorEvento<?> suscriptor : suscriptores) {
            if (suscriptor.tipoDeEvento().isInstance(evento)) {
                entregar(suscriptor, evento);
            }
        }
    }

    private <E extends EventoDominio> void entregar(SuscriptorEvento<E> suscriptor, EventoDominio evento) {
        try {
            suscriptor.alRecibir(suscriptor.tipoDeEvento().cast(evento));
        } catch (RuntimeException e) {
            log.error("El suscriptor {} fallo al procesar {}", suscriptor.getClass().getSimpleName(),
                    evento.getClass().getSimpleName(), e);
        }
    }
}
