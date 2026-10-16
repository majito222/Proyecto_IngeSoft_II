package com.avimanager.domain.service;

import java.util.Collection;

/**
 * Genera codigos legibles y consecutivos ("galpon-5", "lote-4") para las
 * entidades que registra el Administrador (F-11), en lugar de un UUID que no se
 * puede leer en pantalla. Toma el mayor numero existente con ese prefijo y le
 * suma uno, asi un codigo nunca se repite aunque se finalicen lotes.
 */
final class CodigoConsecutivo {

    private CodigoConsecutivo() {
    }

    static String siguiente(String prefijo, Collection<String> idsExistentes) {
        int mayor = idsExistentes.stream()
                .filter(id -> id.startsWith(prefijo + "-"))
                .map(id -> id.substring(prefijo.length() + 1))
                .filter(numero -> numero.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);
        return prefijo + "-" + (mayor + 1);
    }
}
