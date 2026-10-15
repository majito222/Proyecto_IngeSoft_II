package com.avimanager.infrastructure.adapter.in.web.dto;

public class RegistrarGalponRequest {

    private String nombre;
    private Integer capacidadMaxima;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }
}
