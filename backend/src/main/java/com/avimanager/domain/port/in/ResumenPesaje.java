package com.avimanager.domain.port.in;

import com.avimanager.domain.model.Pesaje;

/**
 * Pesaje con los nombres que necesita la pantalla (F-07).
 */
public class ResumenPesaje {

    private final Pesaje pesaje;
    private final String galponNombre;
    private final String registradoPorNombre;
    private final String revisadoPorNombre;

    public ResumenPesaje(Pesaje pesaje, String galponNombre, String registradoPorNombre, String revisadoPorNombre) {
        this.pesaje = pesaje;
        this.galponNombre = galponNombre;
        this.registradoPorNombre = registradoPorNombre;
        this.revisadoPorNombre = revisadoPorNombre;
    }

    public Pesaje getPesaje() {
        return pesaje;
    }

    public String getGalponNombre() {
        return galponNombre;
    }

    public String getRegistradoPorNombre() {
        return registradoPorNombre;
    }

    public String getRevisadoPorNombre() {
        return revisadoPorNombre;
    }
}
