package com.avimanager.domain.port.out;

import com.avimanager.domain.model.Pesaje;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida de los pesajes semanales (F-07). ultimoPesajeDelLote lo usa
 * la racion ajustada por peso (RN-02) y lo puede usar F-04.1 para los kg ganados.
 */
public interface PesajeRepositoryPort {

    Pesaje guardar(Pesaje pesaje);

    Optional<Pesaje> buscarPorId(String pesajeId);

    List<Pesaje> listarTodos();

    List<Pesaje> listarPorLote(String loteId);

    /** El pesaje registrado mas recientemente para el lote, si tiene alguno. */
    Optional<Pesaje> ultimoPesajeDelLote(String loteId);
}
