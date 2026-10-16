package com.avimanager.domain.model;

import java.util.List;

/** Los KPIs de un lote en una fecha (F-04). */
public class KpisLote {

    private final String loteId;
    private final String galponId;
    private final String galponNombre;
    private final int avesVivas;
    private final List<ResultadoKpi> kpis;

    public KpisLote(String loteId, String galponId, String galponNombre, int avesVivas, List<ResultadoKpi> kpis) {
        this.loteId = loteId;
        this.galponId = galponId;
        this.galponNombre = galponNombre;
        this.avesVivas = avesVivas;
        this.kpis = List.copyOf(kpis);
    }

    public String getLoteId() {
        return loteId;
    }

    public String getGalponId() {
        return galponId;
    }

    public String getGalponNombre() {
        return galponNombre;
    }

    public int getAvesVivas() {
        return avesVivas;
    }

    public List<ResultadoKpi> getKpis() {
        return kpis;
    }
}
