package com.avimanager.domain.model;

import com.avimanager.domain.exception.PesajeYaRevisadoException;
import com.avimanager.domain.policy.PoliticaPeso;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de dominio: pesaje semanal de una muestra de aves de un lote (F-07).
 * Al crearse se compara con el peso estandar de la semana de edad del lote; si
 * se desvia mas de +-5% queda PENDIENTE_REVISION para el Zootecnista (RN-02).
 */
public class Pesaje {

    private final String id;
    private final String loteId;
    private final String galponId;
    private final LocalDate fecha;
    private final LocalDateTime fechaHoraRegistro;
    private final int edadSemanas;
    private final double pesoPromedioGramos;
    private final int tamanoMuestra;
    private final double pesoEstandarGramos;
    private final double desviacionPorcentaje;
    private final String registradoPor;
    private EstadoPesaje estado;
    private String revisadoPor;
    private LocalDateTime fechaRevision;

    public Pesaje(String id, Lote lote, double pesoPromedioGramos, int tamanoMuestra,
                  String registradoPor, LocalDateTime fechaHoraRegistro) {
        if (pesoPromedioGramos <= 0) {
            throw new IllegalArgumentException("El peso promedio de la muestra debe ser mayor a cero");
        }
        if (tamanoMuestra <= 0) {
            throw new IllegalArgumentException("El tamaño de la muestra debe ser de al menos un ave");
        }
        if (tamanoMuestra > lote.getPoblacionActual()) {
            throw new IllegalArgumentException("La muestra (" + tamanoMuestra + " aves) no puede ser mayor que las "
                    + lote.getPoblacionActual() + " aves vivas del lote");
        }
        this.id = id;
        this.loteId = lote.getId();
        this.galponId = lote.getGalponId();
        this.fecha = fechaHoraRegistro.toLocalDate();
        this.fechaHoraRegistro = fechaHoraRegistro;
        this.edadSemanas = lote.getEdadSemanas();
        this.pesoPromedioGramos = pesoPromedioGramos;
        this.tamanoMuestra = tamanoMuestra;
        this.pesoEstandarGramos = PoliticaPeso.pesoEstandarGramos(edadSemanas);
        this.desviacionPorcentaje = PoliticaPeso.calcularDesviacion(pesoPromedioGramos, pesoEstandarGramos);
        this.registradoPor = registradoPor;
        this.estado = PoliticaPeso.fueraDeRango(desviacionPorcentaje)
                ? EstadoPesaje.PENDIENTE_REVISION
                : EstadoPesaje.DENTRO_DE_RANGO;
    }

    /** El Zootecnista aprueba ajustar la racion del lote segun este pesaje (RN-02). */
    public void aprobarAjuste(String zootecnistaUsername, LocalDateTime ahora) {
        revisar(EstadoPesaje.AJUSTE_APROBADO, zootecnistaUsername, ahora);
    }

    /** El Zootecnista decide mantener la racion estandar. */
    public void descartarAjuste(String zootecnistaUsername, LocalDateTime ahora) {
        revisar(EstadoPesaje.AJUSTE_DESCARTADO, zootecnistaUsername, ahora);
    }

    private void revisar(EstadoPesaje decision, String zootecnistaUsername, LocalDateTime ahora) {
        if (estado != EstadoPesaje.PENDIENTE_REVISION) {
            throw new PesajeYaRevisadoException(id, estado);
        }
        this.estado = decision;
        this.revisadoPor = zootecnistaUsername;
        this.fechaRevision = ahora;
    }

    public boolean estaFueraDeRango() {
        return PoliticaPeso.fueraDeRango(desviacionPorcentaje);
    }

    public boolean tieneAjusteAprobado() {
        return estado == EstadoPesaje.AJUSTE_APROBADO;
    }

    public String getId() {
        return id;
    }

    public String getLoteId() {
        return loteId;
    }

    public String getGalponId() {
        return galponId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalDateTime getFechaHoraRegistro() {
        return fechaHoraRegistro;
    }

    public int getEdadSemanas() {
        return edadSemanas;
    }

    public double getPesoPromedioGramos() {
        return pesoPromedioGramos;
    }

    public int getTamanoMuestra() {
        return tamanoMuestra;
    }

    public double getPesoEstandarGramos() {
        return pesoEstandarGramos;
    }

    public double getDesviacionPorcentaje() {
        return desviacionPorcentaje;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public EstadoPesaje getEstado() {
        return estado;
    }

    public String getRevisadoPor() {
        return revisadoPor;
    }

    public LocalDateTime getFechaRevision() {
        return fechaRevision;
    }
}
