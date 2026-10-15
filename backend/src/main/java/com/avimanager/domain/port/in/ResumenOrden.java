package com.avimanager.domain.port.in;

import com.avimanager.domain.model.EstadoGalpon;
import com.avimanager.domain.model.OrdenMantenimiento;

/**
 * Orden de mantenimiento con lo que necesita la pantalla (F-08): nombres, estado
 * del galpon y el plazo calculado al momento de la consulta.
 */
public class ResumenOrden {

    private final OrdenMantenimiento orden;
    private final String galponNombre;
    private final EstadoGalpon estadoGalpon;
    private final String reportadoPorNombre;
    private final String tecnicoNombre;
    private final boolean vencida;
    private final long minutosRestantes;

    public ResumenOrden(OrdenMantenimiento orden, String galponNombre, EstadoGalpon estadoGalpon,
                        String reportadoPorNombre, String tecnicoNombre, boolean vencida, long minutosRestantes) {
        this.orden = orden;
        this.galponNombre = galponNombre;
        this.estadoGalpon = estadoGalpon;
        this.reportadoPorNombre = reportadoPorNombre;
        this.tecnicoNombre = tecnicoNombre;
        this.vencida = vencida;
        this.minutosRestantes = minutosRestantes;
    }

    public OrdenMantenimiento getOrden() {
        return orden;
    }

    public String getGalponNombre() {
        return galponNombre;
    }

    public EstadoGalpon getEstadoGalpon() {
        return estadoGalpon;
    }

    public String getReportadoPorNombre() {
        return reportadoPorNombre;
    }

    public String getTecnicoNombre() {
        return tecnicoNombre;
    }

    public boolean isVencida() {
        return vencida;
    }

    public long getMinutosRestantes() {
        return minutosRestantes;
    }
}
