package com.avimanager.domain.model;

import com.avimanager.domain.exception.OrdenNoDisponibleException;

/**
 * Ciclo de vida de una orden de mantenimiento (patron STATE, F-08.3):
 *
 *   ABIERTA --iniciarReparacion--> EN_REPARACION --certificar--> CERTIFICADA
 *      \_____________________certificar_____________________________/
 *
 * Cada constante decide que transiciones admite; las demas lanzan
 * OrdenNoDisponibleException. Las transiciones son package-private: solo
 * OrdenMantenimiento cambia su estado.
 */
public enum EstadoOrden {
    ABIERTA {
        @Override
        EstadoOrden iniciarReparacion() {
            return EN_REPARACION;
        }

        @Override
        EstadoOrden certificar() {
            return CERTIFICADA;
        }
    },
    EN_REPARACION {
        @Override
        EstadoOrden certificar() {
            return CERTIFICADA;
        }
    },
    CERTIFICADA {
        @Override
        public boolean estaPendiente() {
            return false;
        }
    };

    /** Una orden pendiente mantiene el galpon EN_RIESGO_SANITARIO y corre el plazo de 12 h. */
    public boolean estaPendiente() {
        return true;
    }

    EstadoOrden iniciarReparacion() {
        throw transicionNoPermitida("iniciar la reparación de");
    }

    EstadoOrden certificar() {
        throw transicionNoPermitida("certificar");
    }

    private OrdenNoDisponibleException transicionNoPermitida(String accion) {
        return new OrdenNoDisponibleException("No se puede " + accion + " una orden en estado " + name());
    }
}
