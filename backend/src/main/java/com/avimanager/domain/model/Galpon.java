package com.avimanager.domain.model;

/**
 * Entidad de dominio: galpon de la granja (F-11). La capacidad maxima limita la
 * poblacion del lote que aloja. Los galpones nuevos se crean con GalponBuilder,
 * que valida los datos antes de que exista el objeto.
 */
public class Galpon {

    private final String id;
    private final String nombre;
    private final int capacidadMaxima;
    private EstadoGalpon estado;

    public Galpon(String id, String nombre, int capacidadMaxima) {
        this.id = id;
        this.nombre = nombre;
        this.capacidadMaxima = capacidadMaxima;
        this.estado = EstadoGalpon.ACTIVO;
    }

    public static GalponBuilder builder() {
        return new GalponBuilder();
    }

    /** F-08: un dano de infraestructura pone en riesgo a las aves del galpon. */
    public void marcarEnRiesgoSanitario() {
        this.estado = EstadoGalpon.EN_RIESGO_SANITARIO;
    }

    /** F-08: el dano se reparo y el galpon vuelve a operar normalmente. */
    public void restablecer() {
        this.estado = EstadoGalpon.ACTIVO;
    }

    public boolean admitePoblacion(int poblacion) {
        return poblacion <= capacidadMaxima;
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

    public EstadoGalpon getEstado() {
        return estado;
    }
}
