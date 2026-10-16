package com.avimanager.domain.port.out;

import com.avimanager.domain.model.Insumo;
import com.avimanager.domain.model.TipoInsumo;

import java.util.List;
import java.util.Optional;

/** Bodega de la granja (F-03): una sola bodega con un registro por insumo. */
public interface InventarioRepositoryPort {

    Optional<Insumo> buscarPorId(String insumoId);

    /** Insumo de ese tipo en la bodega (hay un alimento, el que descuenta el cierre). */
    Optional<Insumo> buscarPorTipo(TipoInsumo tipo);

    List<Insumo> listarTodos();

    Insumo guardar(Insumo insumo);
}
