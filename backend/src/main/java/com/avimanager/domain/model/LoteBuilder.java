package com.avimanager.domain.model;

import java.time.LocalDate;

import com.avimanager.domain.exception.CapacidadGalponExcedidaException;
import com.avimanager.domain.exception.GalponConLoteActivoException;

/**
 * Patron BUILDER (F-11): arma un Lote nuevo y, en build(), aplica las reglas de
 * creacion antes de que el lote exista:
 *   - la poblacion inicial no puede superar la capacidad maxima del galpon;
 *   - un galpon aloja un solo lote activo a la vez (LoteRepositoryPort.buscarPorGalpon
 *     y el turno del worker dependen de eso).
 * El builder no consulta repositorios (el dominio no depende de nadie): quien lo
 * usa le entrega el galpon y el lote que hoy esta activo en el, si lo hay.
 */
public class LoteBuilder {

    private String id;
    private Galpon galpon;
    private Lote loteActivoDelGalpon;
    private Integer poblacionInicial;
    private Integer edadSemanas;
    private LocalDate fechaIngreso;

    LoteBuilder() {
    }

    public LoteBuilder id(String id) {
        this.id = id;
        return this;
    }

    /**
     * @param galpon              galpon donde se aloja el lote
     * @param loteActivoDelGalpon lote que el galpon aloja hoy, o null si esta libre
     */
    public LoteBuilder alojadoEn(Galpon galpon, Lote loteActivoDelGalpon) {
        this.galpon = galpon;
        this.loteActivoDelGalpon = loteActivoDelGalpon;
        return this;
    }

    public LoteBuilder poblacionInicial(Integer poblacionInicial) {
        this.poblacionInicial = poblacionInicial;
        return this;
    }

    public LoteBuilder edadSemanas(Integer edadSemanas) {
        this.edadSemanas = edadSemanas;
        return this;
    }

    public LoteBuilder ingresoEl(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
        return this;
    }

    public Lote build() {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El lote debe tener un identificador");
        }
        if (galpon == null) {
            throw new IllegalArgumentException("El lote debe alojarse en un galpón");
        }
        if (poblacionInicial == null || poblacionInicial <= 0) {
            throw new IllegalArgumentException("La población inicial del lote debe ser mayor a cero");
        }
        if (edadSemanas == null || edadSemanas < 0) {
            throw new IllegalArgumentException("La edad del lote en semanas no puede ser negativa");
        }
        if (fechaIngreso == null) {
            throw new IllegalArgumentException("La fecha de ingreso del lote es obligatoria");
        }
        if (!galpon.admitePoblacion(poblacionInicial)) {
            throw new CapacidadGalponExcedidaException(galpon, poblacionInicial);
        }
        if (loteActivoDelGalpon != null && loteActivoDelGalpon.estaActivo()) {
            throw new GalponConLoteActivoException(galpon, loteActivoDelGalpon);
        }
        return new Lote(id, galpon.getId(), poblacionInicial, poblacionInicial, edadSemanas, EstadoLote.ACTIVO,
                fechaIngreso);
    }
}
