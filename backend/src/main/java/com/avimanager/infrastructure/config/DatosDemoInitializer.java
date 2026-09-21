package com.avimanager.infrastructure.config;

import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Carga un galpon/lote de ejemplo al arrancar, unicamente para poder probar
 * F-01/F-02 por API sin depender todavia del modulo de gestion de galpones (F-06).
 * IDs fijos para facilitar las pruebas manuales: galpon-1 / lote-1.
 */
@Component
public class DatosDemoInitializer implements CommandLineRunner {

    private final LoteRepositoryPort loteRepository;

    public DatosDemoInitializer(LoteRepositoryPort loteRepository) {
        this.loteRepository = loteRepository;
    }

    @Override
    public void run(String... args) {
        loteRepository.guardar(new Lote("lote-1", "galpon-1", 500, 500, 3, EstadoLote.ACTIVO));
    }
}
