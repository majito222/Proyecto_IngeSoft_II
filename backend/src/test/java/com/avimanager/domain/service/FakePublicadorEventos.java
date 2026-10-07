package com.avimanager.domain.service;

import com.avimanager.domain.event.EventoDominio;
import com.avimanager.domain.port.out.PublicadorEventosPort;

import java.util.ArrayList;
import java.util.List;

/**
 * Doble de prueba del publicador de eventos (patron Observer): guarda los
 * eventos publicados para que la prueba verifique cuales se emitieron.
 */
class FakePublicadorEventos implements PublicadorEventosPort {

    final List<EventoDominio> publicados = new ArrayList<>();

    @Override
    public void publicar(EventoDominio evento) {
        publicados.add(evento);
    }
}
