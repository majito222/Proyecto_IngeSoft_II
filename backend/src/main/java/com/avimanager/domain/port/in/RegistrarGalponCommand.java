package com.avimanager.domain.port.in;

public class RegistrarGalponCommand {

    private final String nombre;
    private final Integer capacidadMaxima;

    public RegistrarGalponCommand(String nombre, Integer capacidadMaxima) {
        this.nombre = nombre;
        this.capacidadMaxima = capacidadMaxima;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getCapacidadMaxima() {
        return capacidadMaxima;
    }
}
