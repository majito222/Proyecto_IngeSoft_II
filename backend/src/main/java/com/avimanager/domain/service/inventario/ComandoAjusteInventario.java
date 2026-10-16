package com.avimanager.domain.service.inventario;

import com.avimanager.domain.model.ActaAjuste;
import com.avimanager.domain.model.Insumo;

import java.time.LocalDateTime;

/**
 * Patron COMMAND (F-05 / RN-08): el Acta de Ajuste. Guarda los datos del ajuste
 * manual (stock contado, justificacion, administrador y fecha) y, al ejecutarse,
 * toma el stock que tenia el sistema, deja en el insumo (el receptor) el stock
 * contado y devuelve el ActaAjuste con la diferencia. Se valida antes de tocar
 * el insumo: si los datos no sirven, el stock no cambia.
 */
public class ComandoAjusteInventario implements ComandoInventario {

    static final int LONGITUD_MINIMA_JUSTIFICACION = 10;

    private final String actaId;
    private final Insumo insumo;
    private final Double stockContado;
    private final String justificacion;
    private final String administradorUsername;
    private final String administradorNombre;
    private final LocalDateTime fecha;

    public ComandoAjusteInventario(String actaId, Insumo insumo, Double stockContado, String justificacion,
                                   String administradorUsername, String administradorNombre, LocalDateTime fecha) {
        this.actaId = actaId;
        this.insumo = insumo;
        this.stockContado = stockContado;
        this.justificacion = justificacion;
        this.administradorUsername = administradorUsername;
        this.administradorNombre = administradorNombre;
        this.fecha = fecha;
    }

    @Override
    public ActaAjuste ejecutar() {
        if (stockContado == null) {
            throw new IllegalArgumentException("El stock contado en la bodega es obligatorio");
        }
        if (justificacion == null || justificacion.trim().length() < LONGITUD_MINIMA_JUSTIFICACION) {
            throw new IllegalArgumentException("La justificación del ajuste es obligatoria (mínimo "
                    + LONGITUD_MINIMA_JUSTIFICACION + " caracteres)");
        }
        double stockSistema = insumo.getStock();
        insumo.ajustarStock(stockContado);
        return new ActaAjuste(actaId, insumo, stockSistema, stockContado, justificacion.trim(),
                administradorUsername, administradorNombre, fecha);
    }
}
