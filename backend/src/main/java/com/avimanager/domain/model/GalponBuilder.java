package com.avimanager.domain.model;

/**
 * Patron BUILDER (F-11): arma un Galpon nuevo paso a paso y valida sus datos en
 * build(), de modo que nunca exista un galpon sin nombre o con capacidad
 * invalida. Se prefiere a un constructor largo porque deja claro que es cada
 * dato y concentra las validaciones de creacion en un solo lugar.
 */
public class GalponBuilder {

    private String id;
    private String nombre;
    private Integer capacidadMaxima;

    GalponBuilder() {
    }

    public GalponBuilder id(String id) {
        this.id = id;
        return this;
    }

    public GalponBuilder nombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public GalponBuilder capacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
        return this;
    }

    public Galpon build() {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El galpón debe tener un identificador");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del galpón es obligatorio");
        }
        if (capacidadMaxima == null || capacidadMaxima <= 0) {
            throw new IllegalArgumentException("La capacidad máxima del galpón debe ser mayor a cero");
        }
        return new Galpon(id, nombre.trim(), capacidadMaxima);
    }
}
