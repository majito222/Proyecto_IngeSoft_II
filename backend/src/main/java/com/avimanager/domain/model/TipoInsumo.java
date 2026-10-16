package com.avimanager.domain.model;

/**
 * Tipos de insumo de la bodega (F-03). El cierre de turno descuenta el
 * ALIMENTO consumido; los demas se mueven con el Acta de Ajuste (F-05).
 */
public enum TipoInsumo {
    ALIMENTO,
    SANIDAD,
    MATERIAL_CAMA
}
