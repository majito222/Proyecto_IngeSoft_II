package com.avimanager.domain.model;

public class Galpon {

    private final String id;
    private final String nombre;
    private final int capacidadMaxima;

    public Galpon(String id, String nombre, int capacidadMaxima) {
        this.id = id;
        this.nombre = nombre;
        this.capacidadMaxima = capacidadMaxima;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }
}
