package com.avimanager.domain.service.mantenimiento;

import com.avimanager.domain.model.OrdenMantenimiento;
import com.avimanager.domain.model.PrioridadOrden;
import com.avimanager.domain.model.TipoDano;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Patron FACTORY para las ordenes de mantenimiento (F-08.1). Concentra en un
 * solo lugar las reglas para crear una orden a partir de un dano:
 *   - prioridad segun el tipo de dano: malla y techo dejan entrar plagas y
 *     rompen el control climatico (ALTA); cortina y otros (MEDIA)
 *   - fecha limite = fecha del reporte + 12 horas (RN-04)
 * El servicio no sabe como se arma la orden; si cambia una regla, solo cambia aqui.
 */
public class FabricaOrdenMantenimiento {

    public static final Duration PLAZO_REPARACION = Duration.ofHours(12);

    public OrdenMantenimiento crear(String galponId, TipoDano tipoDano, String descripcion, String reportadoPor,
                                    LocalDateTime ahora) {
        if (tipoDano == null) {
            throw new IllegalArgumentException("El tipo de daño es obligatorio");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción del daño es obligatoria");
        }
        return new OrdenMantenimiento(UUID.randomUUID().toString(), galponId, tipoDano, descripcion.trim(),
                prioridadPara(tipoDano), reportadoPor, ahora, ahora.plus(PLAZO_REPARACION));
    }

    PrioridadOrden prioridadPara(TipoDano tipoDano) {
        return switch (tipoDano) {
            case ROTURA_MALLA, DANO_TECHO -> PrioridadOrden.ALTA;
            case CORTINA_DANADA, OTRO -> PrioridadOrden.MEDIA;
        };
    }
}
