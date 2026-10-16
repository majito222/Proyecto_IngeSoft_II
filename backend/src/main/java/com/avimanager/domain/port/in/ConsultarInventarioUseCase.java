package com.avimanager.domain.port.in;

import com.avimanager.domain.model.Insumo;

import java.util.List;

/** F-03.2: stock, capacidad y % disponible de cada insumo de la bodega. */
public interface ConsultarInventarioUseCase {

    List<Insumo> listarInsumos();
}
