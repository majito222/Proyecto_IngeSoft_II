package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.port.in.ResumenGalpon;

import java.util.List;

/** F-11.2: galpon con su lote activo (null si esta libre) y sus workers. */
public class GalponResponse {

    private final String id;
    private final String nombre;
    private final int capacidadMaxima;
    private final String estado;
    private final LoteResponse loteActivo;
    private final List<WorkerResponse> workers;

    private GalponResponse(Galpon galpon, LoteResponse loteActivo, List<WorkerResponse> workers) {
        this.id = galpon.getId();
        this.nombre = galpon.getNombre();
        this.capacidadMaxima = galpon.getCapacidadMaxima();
        this.estado = galpon.getEstado().name();
        this.loteActivo = loteActivo;
        this.workers = workers;
    }

    public static GalponResponse desde(ResumenGalpon resumen) {
        return new GalponResponse(resumen.getGalpon(), LoteResponse.desde(resumen.getLoteActivo()),
                resumen.getWorkers().stream().map(WorkerResponse::desde).toList());
    }

    /** Galpon recien registrado: aun no tiene lote ni workers. */
    public static GalponResponse desde(Galpon galpon) {
        return new GalponResponse(galpon, null, List.of());
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

    public String getEstado() {
        return estado;
    }

    public LoteResponse getLoteActivo() {
        return loteActivo;
    }

    public List<WorkerResponse> getWorkers() {
        return workers;
    }
}
