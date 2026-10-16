package com.avimanager.domain.service.kpi;

import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.KpisLote;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.PesajeRepositoryPort;
import com.avimanager.domain.port.out.ReporteDiarioRepositoryPort;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Contexto del patron Strategy (F-04): arma los datos de cada lote activo y le
 * aplica todas las CalculadoraKpi registradas, sin saber cual es cual. Lo usan
 * el dashboard (F-04.2) y la consolidacion del dia (F-04.1).
 */
public class CalculadorKpisLotes {

    private final List<CalculadoraKpi> calculadoras;
    private final LoteRepositoryPort loteRepository;
    private final GalponRepositoryPort galponRepository;
    private final ReporteDiarioRepositoryPort reporteDiarioRepository;
    private final PesajeRepositoryPort pesajeRepository;

    public CalculadorKpisLotes(List<CalculadoraKpi> calculadoras, LoteRepositoryPort loteRepository,
                               GalponRepositoryPort galponRepository,
                               ReporteDiarioRepositoryPort reporteDiarioRepository,
                               PesajeRepositoryPort pesajeRepository) {
        this.calculadoras = List.copyOf(calculadoras);
        this.loteRepository = loteRepository;
        this.galponRepository = galponRepository;
        this.reporteDiarioRepository = reporteDiarioRepository;
        this.pesajeRepository = pesajeRepository;
    }

    public List<KpisLote> calcular(LocalDate fecha) {
        return loteRepository.listarTodos().stream()
                .filter(Lote::estaActivo)
                .map(lote -> calcular(lote, fecha))
                .sorted(Comparator.comparing(KpisLote::getGalponNombre))
                .toList();
    }

    private KpisLote calcular(Lote lote, LocalDate fecha) {
        DatosKpiLote datos = new DatosKpiLote(lote, fecha, reporteDiarioRepository.listarPorLote(lote.getId()),
                pesajeRepository.listarPorLote(lote.getId()));
        String galponNombre = galponRepository.buscarPorId(lote.getGalponId())
                .map(Galpon::getNombre)
                .orElse(lote.getGalponId());
        return new KpisLote(lote.getId(), lote.getGalponId(), galponNombre, lote.getPoblacionActual(),
                calculadoras.stream().map(c -> c.calcular(datos)).toList());
    }
}
