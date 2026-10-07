package com.avimanager.infrastructure.adapter.out.event;

import com.avimanager.domain.event.AlertaSanitariaGenerada;
import com.avimanager.domain.event.EventoDominio;
import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.Lote;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Patron Observer: el publicador entrega cada evento a todos los suscriptores de
 * ese tipo (por ejemplo, Veterinario y Administrador para RN-01), sin Spring.
 */
class PublicadorEventosEnMemoriaTest {

    private static final AlertaSanitariaGenerada ALERTA = new AlertaSanitariaGenerada(
            new AlertaSanitaria("a-1", "lote-1", "r-1", LocalDateTime.of(2026, 3, 10, 13, 0), 2.0),
            new Lote("lote-1", "galpon-1", 500, 500, 3, EstadoLote.EN_OBSERVACION));

    /** Evento de otro tipo, para verificar que los suscriptores solo reciben lo suyo. */
    private static final class OtroEvento implements EventoDominio {
        @Override
        public LocalDateTime ocurridoEn() {
            return LocalDateTime.of(2026, 3, 10, 13, 0);
        }
    }

    private static final class SuscriptorDePrueba implements SuscriptorEvento<AlertaSanitariaGenerada> {
        final List<AlertaSanitariaGenerada> recibidos = new ArrayList<>();

        @Override
        public Class<AlertaSanitariaGenerada> tipoDeEvento() {
            return AlertaSanitariaGenerada.class;
        }

        @Override
        public void alRecibir(AlertaSanitariaGenerada evento) {
            recibidos.add(evento);
        }
    }

    @Test
    void entregaElEventoATodosLosSuscriptoresDeEseTipo() {
        SuscriptorDePrueba veterinario = new SuscriptorDePrueba();
        SuscriptorDePrueba administrador = new SuscriptorDePrueba();
        PublicadorEventosEnMemoria publicador = new PublicadorEventosEnMemoria(List.of(veterinario, administrador));

        publicador.publicar(ALERTA);

        assertThat(veterinario.recibidos).containsExactly(ALERTA);
        assertThat(administrador.recibidos).containsExactly(ALERTA);
    }

    @Test
    void noEntregaEventosDeOtroTipo() {
        SuscriptorDePrueba suscriptor = new SuscriptorDePrueba();
        PublicadorEventosEnMemoria publicador = new PublicadorEventosEnMemoria(List.of(suscriptor));

        publicador.publicar(new OtroEvento());

        assertThat(suscriptor.recibidos).isEmpty();
    }

    @Test
    void siUnSuscriptorFallaLosDemasIgualRecibenElEvento() {
        SuscriptorEvento<AlertaSanitariaGenerada> queFalla = new SuscriptorEvento<>() {
            @Override
            public Class<AlertaSanitariaGenerada> tipoDeEvento() {
                return AlertaSanitariaGenerada.class;
            }

            @Override
            public void alRecibir(AlertaSanitariaGenerada evento) {
                throw new IllegalStateException("canal SMS caido");
            }
        };
        SuscriptorDePrueba administrador = new SuscriptorDePrueba();
        PublicadorEventosEnMemoria publicador = new PublicadorEventosEnMemoria(List.of(queFalla, administrador));

        publicador.publicar(ALERTA);

        assertThat(administrador.recibidos).containsExactly(ALERTA);
    }
}
