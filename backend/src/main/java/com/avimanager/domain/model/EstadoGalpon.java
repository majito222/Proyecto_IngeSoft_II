package com.avimanager.domain.model;

/**
 * Estado operativo de un galpon (F-11). F-08 lo pasa a EN_RIESGO_SANITARIO
 * cuando hay un dano de infraestructura que pone en riesgo a las aves, y lo
 * devuelve a ACTIVO cuando se repara.
 */
public enum EstadoGalpon {
    ACTIVO,
    EN_RIESGO_SANITARIO
}
