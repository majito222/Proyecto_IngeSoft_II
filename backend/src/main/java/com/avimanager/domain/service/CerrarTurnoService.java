package com.avimanager.domain.service;

import com.avimanager.domain.exception.LoteNoEncontradoException;
import com.avimanager.domain.exception.TurnoAjenoException;
import com.avimanager.domain.exception.TurnoNoDisponibleException;
import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.policy.PoliticaMortalidad;
import com.avimanager.domain.port.in.CerrarTurnoCommand;
import com.avimanager.domain.port.in.CerrarTurnoResult;
import com.avimanager.domain.port.in.CerrarTurnoUseCase;
import com.avimanager.domain.port.out.AlertaSanitariaRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.NotificadorAlertaSanitariaPort;
import com.avimanager.domain.port.out.ReporteDiarioRepositoryPort;
import com.avimanager.domain.port.out.TurnoRepositoryPort;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementa F-01 (RN-05) y F-02 (RN-07 / RN-01) en un unico flujo, tal como
 * lo describe la especificacion del caso de uso de negocio "Gestionar turno diario":
 * el registro de mortalidad es un subflujo del cierre de turno.
 * Solo se cierra el turno de hoy del lote, solo si esta ABIERTO y solo su
 * responsable puede cerrarlo (F-10.1).
 *
 * Esta clase no conoce Spring, HTTP ni la forma en que se persisten los datos:
 * solo depende de los puertos del dominio (LoteRepositoryPort, etc.), por lo que
 * es 100% testeable con dobles de prueba simples.
 */
public class CerrarTurnoService implements CerrarTurnoUseCase {

    private final LoteRepositoryPort loteRepository;
    private final ReporteDiarioRepositoryPort reporteDiarioRepository;
    private final AlertaSanitariaRepositoryPort alertaSanitariaRepository;
    private final NotificadorAlertaSanitariaPort notificador;
    private final TurnoRepositoryPort turnoRepository;
    private final Clock clock;

    public CerrarTurnoService(LoteRepositoryPort loteRepository,
                               ReporteDiarioRepositoryPort reporteDiarioRepository,
                               AlertaSanitariaRepositoryPort alertaSanitariaRepository,
                               NotificadorAlertaSanitariaPort notificador,
                               TurnoRepositoryPort turnoRepository,
                               Clock clock) {
        this.loteRepository = loteRepository;
        this.reporteDiarioRepository = reporteDiarioRepository;
        this.alertaSanitariaRepository = alertaSanitariaRepository;
        this.notificador = notificador;
        this.turnoRepository = turnoRepository;
        this.clock = clock;
    }

    @Override
    public CerrarTurnoResult cerrarTurno(CerrarTurnoCommand comando) {
        Lote lote = loteRepository.buscarPorId(comando.getLoteId())
                .orElseThrow(() -> new LoteNoEncontradoException(comando.getLoteId()));

        Turno turno = turnoRepository.buscarPorLoteYFecha(lote.getId(), LocalDate.now(clock))
                .orElseThrow(() -> new TurnoNoDisponibleException("Debes iniciar el turno de hoy antes de cerrarlo"));
        if (!turno.esResponsable(comando.getSolicitanteUsername())) {
            throw new TurnoAjenoException(turno.getWorkerUsername());
        }
        if (!turno.estaAbierto()) {
            throw new TurnoNoDisponibleException("El turno de hoy ya no está abierto (estado " + turno.getEstado() + ")");
        }

        List<String> camposFaltantes = validarCamposObligatorios(comando);
        if (!camposFaltantes.isEmpty()) {
            return CerrarTurnoResult.camposIncompletos(camposFaltantes, turno);
        }

        ReporteDiario reporte = new ReporteDiario(
                UUID.randomUUID().toString(),
                lote.getId(),
                LocalDate.now(clock),
                comando.getConsumoAlimentoKg(),
                comando.getCantidadBajas(),
                comando.getCausaProbableMortalidad(),
                comando.getProduccionHuevosBandejas(),
                comando.getNovedades()
        );

        double porcentajeMortalidad = PoliticaMortalidad.calcularPorcentaje(
                comando.getCantidadBajas(), lote.getPoblacionInicialDia());
        reporte.registrarPorcentajeMortalidad(porcentajeMortalidad);

        if (PoliticaMortalidad.superaUmbralCritico(porcentajeMortalidad)) {
            reporte.bloquearPorAlertaSanitaria(porcentajeMortalidad);
            reporteDiarioRepository.guardar(reporte);

            lote.ponerEnObservacion();
            loteRepository.guardar(lote);

            AlertaSanitaria alerta = new AlertaSanitaria(
                    UUID.randomUUID().toString(),
                    lote.getId(),
                    reporte.getId(),
                    LocalDateTime.now(clock),
                    porcentajeMortalidad
            );
            alertaSanitariaRepository.guardar(alerta);
            notificador.notificarAlVeterinario(alerta, lote);

            turno.bloquearPorAlertaSanitaria(reporte, alerta);
            turnoRepository.guardar(turno);

            return CerrarTurnoResult.bloqueadoPorAlertaSanitaria(reporte, alerta, turno);
        }

        lote.registrarBajas(comando.getCantidadBajas());
        loteRepository.guardar(lote);

        reporte.consolidar();
        reporteDiarioRepository.guardar(reporte);

        turno.cerrar(reporte, LocalDateTime.now(clock));
        turnoRepository.guardar(turno);

        return CerrarTurnoResult.turnoCerrado(reporte, turno);
    }

    private List<String> validarCamposObligatorios(CerrarTurnoCommand comando) {
        List<String> faltantes = new ArrayList<>();
        if (comando.getConsumoAlimentoKg() == null) {
            faltantes.add("consumoAlimentoKg");
        }
        if (comando.getCantidadBajas() == null) {
            faltantes.add("cantidadBajas");
        }
        if (comando.getCantidadBajas() != null && comando.getCantidadBajas() > 0
                && (comando.getCausaProbableMortalidad() == null || comando.getCausaProbableMortalidad().isBlank())) {
            faltantes.add("causaProbableMortalidad");
        }
        if (comando.getProduccionHuevosBandejas() == null) {
            faltantes.add("produccionHuevosBandejas");
        }
        return faltantes;
    }
}
