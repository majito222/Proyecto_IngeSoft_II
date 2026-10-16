package com.avimanager.domain.model;

import java.text.Collator;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.Locale;

/**
 * Entidad de dominio: galpon de la granja (F-11). La capacidad maxima limita la
 * poblacion del lote que aloja. Los galpones nuevos se crean con GalponBuilder,
 * que valida los datos antes de que exista el objeto.
 */
public class Galpon {

    /**
     * Orden alfabetico en espanol: ignora tildes y mayusculas, asi "Galpon 4"
     * no queda antes que "Galpón 1".
     */
    public static final Comparator<String> ORDEN_POR_NOMBRE = crearOrdenPorNombre();

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

    /** Dos nombres de galpon son el mismo si solo cambian mayusculas, tildes o espacios. */
    public boolean tieneElNombre(String otroNombre) {
        return otroNombre != null && normalizar(nombre).equals(normalizar(otroNombre));
    }

    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static Comparator<String> crearOrdenPorNombre() {
        Collator collator = Collator.getInstance(Locale.forLanguageTag("es"));
        collator.setStrength(Collator.PRIMARY);
        return collator::compare;
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
