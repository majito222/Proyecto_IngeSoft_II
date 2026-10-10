package com.avimanager.domain.service;

import com.avimanager.domain.exception.LoteNoEncontradoException;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.port.in.CalcularRacionUseCase;
import com.avimanager.domain.port.in.RacionDiaria;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.service.racion.EstrategiaRacion;
import com.avimanager.domain.service.racion.SelectorEstrategiaRacion;

import java.util.Comparator;
import java.util.List;

/**
 * Implementa F-06: racion del dia = aves vivas x gramos por ave. Los gramos por
 * ave los decide la estrategia que elige SelectorEstrategiaRacion (patron
 * Strategy); este servicio no sabe cual se uso.
 */
public class CalcularRacionService implements CalcularRacionUseCase {

    private final LoteRepositoryPort loteRepository;
    private final GalponRepositoryPort galponRepository;
    private final SelectorEstrategiaRacion selector;

    public CalcularRacionService(LoteRepositoryPort loteRepository, GalponRepositoryPort galponRepository,
                                 SelectorEstrategiaRacion selector) {
        this.loteRepository = loteRepository;
        this.galponRepository = galponRepository;
        this.selector = selector;
    }

    @Override
    public List<RacionDiaria> racionesDelDia() {
        return loteRepository.listarTodos().stream()
                .map(this::calcular)
                .sorted(Comparator.comparing(RacionDiaria::getGalponId))
                .toList();
    }

    @Override
    public RacionDiaria racionDelLote(String loteId) {
        return calcular(loteRepository.buscarPorId(loteId).orElseThrow(() -> new LoteNoEncontradoException(loteId)));
    }

    private RacionDiaria calcular(Lote lote) {
        EstrategiaRacion estrategia = selector.para(lote);
        double gramosPorAve = estrategia.gramosPorAve(lote);
        double kilos = Math.round(lote.getPoblacionActual() * gramosPorAve / 100.0) / 10.0;
        String galponNombre = galponRepository.buscarPorId(lote.getGalponId())
                .map(Galpon::getNombre)
                .orElse(lote.getGalponId());
        return new RacionDiaria(lote.getGalponId(), galponNombre, lote.getId(), lote.getEdadSemanas(),
                lote.getPoblacionActual(), gramosPorAve, kilos, estrategia.nombre());
    }
}
