package com.avimanager.domain.model;

/**
 * Ciclo de vida del turno de un worker (F-10):
 *   ABIERTO -> CERRADO                     cierre exitoso
 *   ABIERTO -> BLOQUEADO_ALERTA_SANITARIA  la mortalidad supero el umbral (RN-01)
 *   BLOQUEADO_ALERTA_SANITARIA -> CERRADO_CON_ALERTA  el veterinario atiende la alerta (F-02.5)
 *   CERRADO / CERRADO_CON_ALERTA -> APROBADO          el administrador aprueba el reporte (F-04.3)
 * Un intento de cierre con campos faltantes (RN-05) deja el turno ABIERTO.
 */
public enum EstadoTurno {
    ABIERTO,
    BLOQUEADO_ALERTA_SANITARIA,
    CERRADO,
    CERRADO_CON_ALERTA,
    APROBADO
}
