package com.avimanager.domain.service;

import com.avimanager.domain.exception.InventarioNoConciliadoException;
import com.avimanager.domain.exception.ReporteYaConsolidadoException;
import com.avimanager.domain.exception.TurnosSinAprobarException;
import com.avimanager.domain.model.DiferenciaInventario;
import com.avimanager.domain.model.ReporteConsolidado;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.ConsolidarReporteCommand;
import com.avimanager.domain.port.in.ConsolidarReporteDiarioUseCase;
import com.avimanager.domain.policy.PoliticaConciliacion;
import com.avimanager.domain.port.out.InventarioRepositoryPort;
import com.avimanager.domain.port.out.ReporteConsolidadoRepositoryPort;
import com.avimanager.domain.port.out.TurnoRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;
import com.avimanager.domain.service.kpi.CalculadorKpisLotes;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementa F-04.1: consolida el reporte del dia con los KPIs de cada lote
 * (calculados con el patron Strategy). Antes exige que la fecha no este
 * consolidada ya, que todos los turnos de esa fecha esten APROBADOS (F-04.3) y
 * que el conteo fisico de la bodega cuadre con el sistema (RN-06); si no cuadra,
 * el Administrador debe registrar un Acta de Ajuste (F-05) y volver a consolidar.
 */
public class ConsolidarReporteDiarioService implements ConsolidarReporteDiarioUseCase {

    private final TurnoRepositoryPort turnoRepository;
    private final ReporteConsolidadoRepositoryPort reporteConsolidadoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final InventarioRepositoryPort inventarioRepository;
    private final CalculadorKpisLotes calculadorKpis;
    private final Clock clock;

    public ConsolidarReporteDiarioService(TurnoRepositoryPort turnoRepository,
                                          ReporteConsolidadoRepositoryPort reporteConsolidadoRepository,
                                          UsuarioRepositoryPort usuarioRepository,
                                          InventarioRepositoryPort inventarioRepository,
                                          CalculadorKpisLotes calculadorKpis,
                                          Clock clock) {
        this.turnoRepository = turnoRepository;
        this.reporteConsolidadoRepository = reporteConsolidadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.inventarioRepository = inventarioRepository;
        this.calculadorKpis = calculadorKpis;
        this.clock = clock;
    }

    @Override
    public ReporteConsolidado consolidar(ConsolidarReporteCommand comando) {
        LocalDate fecha = comando.getFecha();
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha a consolidar es obligatoria");
        }
        if (fecha.isAfter(LocalDate.now(clock))) {
            throw new IllegalArgumentException("No se puede consolidar una fecha futura");
        }
        if (reporteConsolidadoRepository.buscarPorFecha(fecha).isPresent()) {
            throw new ReporteYaConsolidadoException(fecha);
        }

        List<Turno> turnosDelDia = turnoRepository.listarTodos().stream()
                .filter(t -> t.getFecha().equals(fecha))
                .toList();
        long sinAprobar = turnosDelDia.stream().filter(t -> !t.estaAprobado()).count();
        if (sinAprobar > 0) {
            throw new TurnosSinAprobarException(fecha, sinAprobar);
        }

        // RN-06: el cierre administrativo se bloquea si la bodega no cuadra con el sistema.
        List<DiferenciaInventario> diferencias = PoliticaConciliacion.diferencias(
                inventarioRepository.listarTodos(), comando.getConteoFisico());
        if (!diferencias.isEmpty()) {
            throw new InventarioNoConciliadoException(fecha, diferencias);
        }

        String nombre = usuarioRepository.buscarPorUsername(comando.getAdministradorUsername())
                .map(Usuario::getNombreCompleto)
                .orElse(comando.getAdministradorUsername());
        ReporteConsolidado reporte = new ReporteConsolidado(UUID.randomUUID().toString(), fecha,
                comando.getAdministradorUsername(), nombre, LocalDateTime.now(clock), turnosDelDia.size(),
                calculadorKpis.calcular(fecha), comando.getConteoFisico());
        return reporteConsolidadoRepository.guardar(reporte);
    }
}
