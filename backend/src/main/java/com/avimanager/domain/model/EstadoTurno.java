package com.avimanager.domain.model;

import com.avimanager.domain.exception.TurnoNoDisponibleException;

/**
 * Ciclo de vida del turno de un worker (F-10), implementado con el patron STATE:
 * cada estado decide que transiciones admite y a que estado lleva cada una. Una
 * transicion que el estado no admite lanza TurnoNoDisponibleException, asi que
 * Turno no necesita "if (estado == ...)" repartidos.
 *
 *   ABIERTO -> CERRADO                     cierre exitoso
 *   ABIERTO -> BLOQUEADO_ALERTA_SANITARIA  la mortalidad supero el umbral (RN-01)
 *   BLOQUEADO_ALERTA_SANITARIA -> CERRADO_CON_ALERTA  el veterinario atiende la alerta (F-02.5)
 *   CERRADO / CERRADO_CON_ALERTA -> APROBADO          el administrador aprueba el reporte (F-04.3)
 * Un intento de cierre con campos faltantes (RN-05) deja el turno ABIERTO.
 *
 * Las transiciones son package-private: solo Turno puede cambiar su estado.
 */
public enum EstadoTurno {

    ABIERTO {
        @Override
        public boolean admiteCierre() {
            return true;
        }

        @Override
        EstadoTurno cerrar() {
            return CERRADO;
        }

        @Override
        EstadoTurno bloquearPorAlertaSanitaria() {
            return BLOQUEADO_ALERTA_SANITARIA;
        }
    },

    BLOQUEADO_ALERTA_SANITARIA {
        @Override
        EstadoTurno cerrarConAlertaAtendida() {
            return CERRADO_CON_ALERTA;
        }
    },

    CERRADO {
        @Override
        EstadoTurno aprobar() {
            return APROBADO;
        }
    },

    CERRADO_CON_ALERTA {
        @Override
        EstadoTurno aprobar() {
            return APROBADO;
        }
    },

    APROBADO;

    /** Solo un turno ABIERTO recibe el reporte del worker. */
    public boolean admiteCierre() {
        return false;
    }

    EstadoTurno cerrar() {
        throw transicionNoPermitida("cerrar");
    }

    EstadoTurno bloquearPorAlertaSanitaria() {
        throw transicionNoPermitida("bloquear por alerta sanitaria");
    }

    EstadoTurno cerrarConAlertaAtendida() {
        throw transicionNoPermitida("cerrar con alerta atendida");
    }

    EstadoTurno aprobar() {
        throw transicionNoPermitida("aprobar");
    }

    private TurnoNoDisponibleException transicionNoPermitida(String accion) {
        return new TurnoNoDisponibleException("No se puede " + accion + " un turno en estado " + name());
    }
}
