package com.avimanager.domain.model;

import com.avimanager.domain.exception.LoteYaFinalizadoException;

import java.time.LocalDate;

/**
 * Entidad de dominio: Lote de aves dentro de un galpon.
 * poblacionInicial es la poblacion con la que ingreso el lote (base de la
 * mortalidad acumulada, F-04). poblacionActual son las aves vivas: como hay un
 * solo turno por lote al dia y las bajas se descuentan al consolidar el cierre,
 * al momento del cierre es tambien la poblacion inicial del dia (RN-07).
 * Los lotes nuevos se crean con LoteBuilder (F-11), que valida la capacidad del
 * galpon y que este no tenga ya un lote activo.
 */
public class Lote {

    private final String id;
    private final String galponId;
    private final int poblacionInicial;
    private int poblacionActual;
    private final int edadSemanas;
    private EstadoLote estado;
    private final LocalDate fechaIngreso;

    public Lote(String id, String galponId, int poblacionInicial, int poblacionActual, int edadSemanas, EstadoLote estado) {
        this(id, galponId, poblacionInicial, poblacionActual, edadSemanas, estado, null);
    }

    /** @param fechaIngreso dia en que el lote llego al galpon; null si no se conoce (existe desde siempre) */
    public Lote(String id, String galponId, int poblacionInicial, int poblacionActual, int edadSemanas, EstadoLote estado,
                LocalDate fechaIngreso) {
        this.id = id;
        this.galponId = galponId;
        this.poblacionInicial = poblacionInicial;
        this.poblacionActual = poblacionActual;
        this.edadSemanas = edadSemanas;
        this.estado = estado;
        this.fechaIngreso = fechaIngreso;
    }

    /** F-04: un lote solo aparece en los indicadores de los dias en que ya estaba en el galpon. */
    public boolean existiaEl(LocalDate fecha) {
        return fechaIngreso == null || !fechaIngreso.isAfter(fecha);
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public static LoteBuilder builder() {
        return new LoteBuilder();
    }

    /**
     * Consolida el reporte diario del lote y descuenta sus bajas. Es la unica forma de
     * descontar bajas: un reporte consolidado siempre las desconto (cierre normal,
     * atencion de la alerta sanitaria y datos de demo usan este metodo).
     * La invariante 0 <= bajas <= aves vivas se protege aqui; los caminos validos ya la
     * cumplen (ReglaValoresValidos la revisa al cerrar), asi que violarla es un error interno.
     */
    public void consolidarReporte(ReporteDiario reporte) {
        if (!id.equals(reporte.getLoteId())) {
            throw new IllegalStateException("El reporte " + reporte.getId() + " no es del lote " + id);
        }
        Integer bajas = reporte.getCantidadBajas();
        if (bajas == null || bajas < 0 || bajas > poblacionActual) {
            throw new IllegalStateException("Las bajas del reporte (" + bajas + ") deben estar entre 0 y las "
                    + poblacionActual + " aves vivas del lote " + id);
        }
        reporte.consolidar();
        registrarBajas(bajas);
    }

    private void registrarBajas(int cantidadBajas) {
        this.poblacionActual = this.poblacionActual - cantidadBajas;
    }

    public void ponerEnObservacion() {
        this.estado = EstadoLote.EN_OBSERVACION;
    }

    /** El veterinario decide sacar el lote de observacion al atender la alerta (F-02.5). */
    public void liberarDeObservacion() {
        this.estado = EstadoLote.ACTIVO;
    }

    /** F-11: el lote sale del galpon, que queda libre para recibir un lote nuevo. */
    public void finalizar() {
        if (!estaActivo()) {
            throw new LoteYaFinalizadoException(id);
        }
        this.estado = EstadoLote.FINALIZADO;
    }

    /** Un lote esta activo mientras siga alojado en su galpon (aunque este en observacion). */
    public boolean estaActivo() {
        return estado != EstadoLote.FINALIZADO;
    }

    public String getId() {
        return id;
    }

    public String getGalponId() {
        return galponId;
    }

    public int getPoblacionInicial() {
        return poblacionInicial;
    }

    public int getPoblacionActual() {
        return poblacionActual;
    }

    public int getEdadSemanas() {
        return edadSemanas;
    }

    public EstadoLote getEstado() {
        return estado;
    }
}
