package com.avimanager.domain.model;

import com.avimanager.domain.policy.PoliticaStock;

/**
 * Entidad de dominio: insumo de la bodega de la granja (F-03), con su stock
 * actual y la capacidad de almacenamiento. El cierre de turno descuenta el
 * alimento consumido (RN-03); el stock nunca queda negativo. Solo un Acta de
 * Ajuste (F-05) cambia el stock a mano.
 */
public class Insumo {

    private final String id;
    private final String nombre;
    private final TipoInsumo tipo;
    private final String unidad;
    private final double capacidad;
    private double stock;

    public Insumo(String id, String nombre, TipoInsumo tipo, String unidad, double capacidad, double stock) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad de la bodega para " + nombre + " debe ser mayor a cero");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock de " + nombre + " no puede ser negativo");
        }
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.unidad = unidad;
        this.capacidad = capacidad;
        this.stock = stock;
    }

    /**
     * Descuenta lo consumido. Si el consumo reportado supera el stock, el stock
     * queda en 0 (el faltante se corrige con el Acta de Ajuste de F-05).
     */
    public void descontar(double cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad a descontar no puede ser negativa");
        }
        this.stock = Math.max(0, stock - cantidad);
    }

    /**
     * F-05: reemplaza el stock del sistema por el contado en la bodega. Solo lo
     * usa el Acta de Ajuste (ComandoAjusteInventario), que deja la auditoria.
     */
    public void ajustarStock(double stockContado) {
        if (stockContado < 0) {
            throw new IllegalArgumentException("El stock contado no puede ser negativo");
        }
        if (stockContado > capacidad) {
            throw new IllegalArgumentException("El stock contado (" + stockContado + " " + unidad
                    + ") supera la capacidad de la bodega para " + nombre + " (" + capacidad + " " + unidad + ")");
        }
        this.stock = stockContado;
    }

    public double porcentajeDisponible() {
        return PoliticaStock.porcentajeDisponible(stock, capacidad);
    }

    public boolean esCritico() {
        return PoliticaStock.esCritico(stock, capacidad);
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoInsumo getTipo() {
        return tipo;
    }

    public String getUnidad() {
        return unidad;
    }

    public double getCapacidad() {
        return capacidad;
    }

    public double getStock() {
        return stock;
    }
}
