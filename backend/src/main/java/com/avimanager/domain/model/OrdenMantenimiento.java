package com.avimanager.domain.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Entidad de dominio: orden de mantenimiento creada al reportar un dano de
 * infraestructura (F-08). Se crea con FabricaOrdenMantenimiento, que asigna la
 * prioridad y la fecha limite de 12 horas (RN-04). Su ciclo de vida lo decide
 * EstadoOrden (patron State).
 */
public class OrdenMantenimiento {

    private final String id;
    private final String galponId;
    private final TipoDano tipoDano;
    private final String descripcion;
    private final PrioridadOrden prioridad;
    private final String reportadoPor;
    private final LocalDateTime fechaReporte;
    private final LocalDateTime fechaLimite;
    private EstadoOrden estado;
    private String tecnico;
    private LocalDateTime fechaInicioReparacion;
    private String notaReparacion;
    private LocalDateTime fechaCertificacion;

    public OrdenMantenimiento(String id, String galponId, TipoDano tipoDano, String descripcion,
                              PrioridadOrden prioridad, String reportadoPor, LocalDateTime fechaReporte,
                              LocalDateTime fechaLimite) {
        this.id = id;
        this.galponId = galponId;
        this.tipoDano = tipoDano;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.reportadoPor = reportadoPor;
        this.fechaReporte = fechaReporte;
        this.fechaLimite = fechaLimite;
        this.estado = EstadoOrden.ABIERTA;
    }

    /** El tecnico llega al galpon y empieza a reparar. */
    public void iniciarReparacion(String tecnicoUsername, LocalDateTime ahora) {
        this.estado = estado.iniciarReparacion();
        this.tecnico = tecnicoUsername;
        this.fechaInicioReparacion = ahora;
    }

    /** El tecnico certifica la reparacion; la nota deja constancia de lo que se hizo. */
    public void certificar(String tecnicoUsername, String nota, LocalDateTime ahora) {
        // Primero el estado: certificar una orden ya certificada es un conflicto, no un dato faltante.
        EstadoOrden siguiente = estado.certificar();
        if (nota == null || nota.isBlank()) {
            throw new IllegalArgumentException("La nota de reparación es obligatoria para certificar la orden");
        }
        this.estado = siguiente;
        this.tecnico = tecnicoUsername;
        this.notaReparacion = nota.trim();
        this.fechaCertificacion = ahora;
    }

    public boolean estaPendiente() {
        return estado.estaPendiente();
    }

    /** RN-04: una orden pendiente vence cuando pasan las 12 horas del plazo. */
    public boolean estaVencida(LocalDateTime ahora) {
        return estaPendiente() && ahora.isAfter(fechaLimite);
    }

    /** Minutos que faltan para la fecha limite (negativo si ya vencio). */
    public long minutosRestantes(LocalDateTime ahora) {
        return Duration.between(ahora, fechaLimite).toMinutes();
    }

    public String getId() {
        return id;
    }

    public String getGalponId() {
        return galponId;
    }

    public TipoDano getTipoDano() {
        return tipoDano;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public PrioridadOrden getPrioridad() {
        return prioridad;
    }

    public String getReportadoPor() {
        return reportadoPor;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public LocalDateTime getFechaLimite() {
        return fechaLimite;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public String getTecnico() {
        return tecnico;
    }

    public LocalDateTime getFechaInicioReparacion() {
        return fechaInicioReparacion;
    }

    public String getNotaReparacion() {
        return notaReparacion;
    }

    public LocalDateTime getFechaCertificacion() {
        return fechaCertificacion;
    }
}
