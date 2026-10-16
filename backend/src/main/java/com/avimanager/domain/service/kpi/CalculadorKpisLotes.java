package com.avimanager.domain.service.kpi;

import com.avimanager.domain.model.EstadoReporte;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.KpisLote;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.ReporteDiario;
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
                .filter(lote -> lote.existiaEl(fecha))
                .map(lote -> calcular(lote, fecha))
                .sorted(Comparator.comparing(KpisLote::getGalponNombre, Galpon.ORDEN_POR_NOMBRE))
                .toList();
    }

    private KpisLote calcular(Lote lote, LocalDate fecha) {
        List<ReporteDiario> reportes = reporteDiarioRepository.listarPorLote(lote.getId());
        int avesVivas = avesVivasAl(lote, fecha, reportes);
        DatosKpiLote datos = new DatosKpiLote(lote, fecha, reportes, pesajeRepository.listarPorLote(lote.getId()),
                avesVivas);
        String galponNombre = galponRepository.buscarPorId(lote.getGalponId())
                .map(Galpon::getNombre)
                .orElse(lote.getGalponId());
        return new KpisLote(lote.getId(), lote.getGalponId(), galponNombre, avesVivas,
                calculadoras.stream().map(c -> c.calcular(datos)).toList());
    }

    /**
     * Aves vivas al terminar la fecha consultada: las de hoy mas las bajas que se
     * descontaron en dias posteriores (reportes consolidados despues de esa fecha).
     * Para hoy coincide con la poblacion actual del lote.
     */
    private static int avesVivasAl(Lote lote, LocalDate fecha, List<ReporteDiario> reportes) {
        int bajasPosteriores = reportes.stream()
                .filter(r -> r.getEstado() == EstadoReporte.CONSOLIDADO && r.getFecha().isAfter(fecha))
                .mapToInt(r -> r.getCantidadBajas() == null ? 0 : r.getCantidadBajas())
                .sum();
        return lote.getPoblacionActual() + bajasPosteriores;
    }
}
