package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.model.KpisLote;
import com.avimanager.domain.model.ResultadoKpi;

import java.util.List;

/** F-04.2: los KPIs de un lote. */
public class KpisLoteResponse {

    private final String loteId;
    private final String galponId;
    private final String galponNombre;
    private final int avesVivas;
    private final List<Kpi> kpis;

    private KpisLoteResponse(KpisLote lote) {
        this.loteId = lote.getLoteId();
        this.galponId = lote.getGalponId();
        this.galponNombre = lote.getGalponNombre();
        this.avesVivas = lote.getAvesVivas();
        this.kpis = lote.getKpis().stream().map(Kpi::new).toList();
    }

    public static KpisLoteResponse desde(KpisLote lote) {
        return new KpisLoteResponse(lote);
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

    public List<Kpi> getKpis() {
        return kpis;
    }

    public static class Kpi {

        private final String clave;
        private final String nombre;
        private final String unidad;
        private final Double valor;
        private final boolean estimado;
        private final String detalle;

        Kpi(ResultadoKpi kpi) {
            this.clave = kpi.getClave();
            this.nombre = kpi.getNombre();
            this.unidad = kpi.getUnidad();
            this.valor = kpi.getValor();
            this.estimado = kpi.isEstimado();
            this.detalle = kpi.getDetalle();
        }

        public String getClave() {
            return clave;
        }

        public String getNombre() {
            return nombre;
        }

        public String getUnidad() {
            return unidad;
        }

        public Double getValor() {
            return valor;
        }

        public boolean isEstimado() {
            return estimado;
        }

        public String getDetalle() {
            return detalle;
        }
    }
}
