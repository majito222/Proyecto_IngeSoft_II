package com.avimanager.domain.model;

/**
 * Valor de un KPI para un lote (F-04). valor es null cuando no hay datos para
 * calcularlo (detalle explica por que). estimado indica que se uso un dato de
 * referencia en lugar de uno medido (por ejemplo, el peso estandar).
 */
public class ResultadoKpi {

    private final String clave;
    private final String nombre;
    private final String unidad;
    private final Double valor;
    private final boolean estimado;
    private final String detalle;

    public ResultadoKpi(String clave, String nombre, String unidad, Double valor, boolean estimado, String detalle) {
        this.clave = clave;
        this.nombre = nombre;
        this.unidad = unidad;
        this.valor = valor;
        this.estimado = estimado;
        this.detalle = detalle;
    }

    public String getClave() {
        return clave;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUnidad() {
        return unidad;
    }

    public Double getValor() {
        return valor;
    }

    public boolean isEstimado() {
        return estimado;
    }

    public String getDetalle() {
        return detalle;
    }
}
