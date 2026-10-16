package com.avimanager.domain.model;

import com.avimanager.domain.exception.OrdenNoDisponibleException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-08.3 / F-08.4: ciclo de la orden (patron State) y plazo de 12 horas (RN-04).
 */
class OrdenMantenimientoTest {

    private static final LocalDateTime REPORTE = LocalDateTime.of(2026, 3, 10, 8, 0);

    private OrdenMantenimiento orden() {
        return new OrdenMantenimiento("o-1", "galpon-1", TipoDano.ROTURA_MALLA, "Malla rota", PrioridadOrden.ALTA,
                "operario", REPORTE, REPORTE.plusHours(12));
    }

    @Test
    void naceAbiertaYPendiente() {
        OrdenMantenimiento o = orden();

        assertThat(o.getEstado()).isEqualTo(EstadoOrden.ABIERTA);
        assertThat(o.estaPendiente()).isTrue();
    }

    @Test
    void elTecnicoIniciaYCertificaLaReparacion() {
        OrdenMantenimiento o = orden();

        o.iniciarReparacion("tecnico", REPORTE.plusHours(2));
        assertThat(o.getEstado()).isEqualTo(EstadoOrden.EN_REPARACION);
        assertThat(o.estaPendiente()).as("en reparacion sigue pendiente").isTrue();

        o.certificar("tecnico", "  Se cambió la malla  ", REPORTE.plusHours(5));
        assertThat(o.getEstado()).isEqualTo(EstadoOrden.CERTIFICADA);
        assertThat(o.estaPendiente()).isFalse();
        assertThat(o.getNotaReparacion()).isEqualTo("Se cambió la malla");
        assertThat(o.getTecnico()).isEqualTo("tecnico");
        assertThat(o.getFechaCertificacion()).isEqualTo(REPORTE.plusHours(5));
    }

    @Test
    void sePuedeCertificarDirectamenteDesdeAbierta() {
        OrdenMantenimiento o = orden();

        o.certificar("tecnico", "Reparada", REPORTE.plusHours(1));

        assertThat(o.getEstado()).isEqualTo(EstadoOrden.CERTIFICADA);
    }

    @Test
    void transicionesNoPermitidas() {
        OrdenMantenimiento o = orden();
        o.iniciarReparacion("tecnico", REPORTE);
        assertThatThrownBy(() -> o.iniciarReparacion("tecnico", REPORTE))
                .isInstanceOf(OrdenNoDisponibleException.class)
                .hasMessageContaining("EN_REPARACION");

        o.certificar("tecnico", "Reparada", REPORTE);
        assertThatThrownBy(() -> o.certificar("tecnico", "Otra vez", REPORTE))
                .isInstanceOf(OrdenNoDisponibleException.class)
                .hasMessageContaining("CERTIFICADA");
        assertThatThrownBy(() -> o.certificar("tecnico", "", REPORTE))
                .as("una orden certificada es conflicto aunque falte la nota")
                .isInstanceOf(OrdenNoDisponibleException.class);
    }

    @Test
    void certificarExigeLaNotaDeReparacion() {
        OrdenMantenimiento o = orden();

        assertThatThrownBy(() -> o.certificar("tecnico", " ", REPORTE)).isInstanceOf(IllegalArgumentException.class);
        assertThat(o.getEstado()).isEqualTo(EstadoOrden.ABIERTA);
    }

    @Test
    void venceCuandoPasanLas12HorasSinCertificar_RN04() {
        OrdenMantenimiento o = orden();

        assertThat(o.estaVencida(REPORTE.plusHours(11))).isFalse();
        assertThat(o.minutosRestantes(REPORTE.plusHours(11))).isEqualTo(60);
        assertThat(o.estaVencida(REPORTE.plusHours(12))).as("justo en el limite aun no vence").isFalse();
        assertThat(o.estaVencida(REPORTE.plusHours(14))).isTrue();
        assertThat(o.minutosRestantes(REPORTE.plusHours(14))).isEqualTo(-120);
    }

    @Test
    void losMinutosRestantesSeRedondeanHaciaArriba() {
        OrdenMantenimiento o = orden();

        assertThat(o.minutosRestantes(REPORTE.plusNanos(1_000))).as("recien creada: 12 h completas").isEqualTo(720);
        assertThat(o.minutosRestantes(REPORTE.plusMinutes(30).plusSeconds(1))).isEqualTo(690);
        assertThat(o.minutosRestantes(REPORTE.plusHours(12).plusSeconds(30))).as("vencida hace 30 s").isZero();
    }

    @Test
    void unaOrdenCertificadaNoVence() {
        OrdenMantenimiento o = orden();
        o.certificar("tecnico", "Reparada", REPORTE.plusHours(13));

        assertThat(o.estaVencida(REPORTE.plusHours(20))).isFalse();
    }
}
