package com.avimanager.domain.service;

import com.avimanager.domain.exception.LoteNoEncontradoException;
import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.policy.PoliticaMortalidad;
import com.avimanager.domain.port.in.CerrarTurnoCommand;
import com.avimanager.domain.port.in.CerrarTurnoResult;
import com.avimanager.domain.port.in.CerrarTurnoUseCase;
import com.avimanager.domain.port.out.AlertaSanitariaRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.NotificadorAlertaSanitariaPort;
import com.avimanager.domain.port.out.ReporteDiarioRepositoryPort;

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
    private final Clock clock;

    public CerrarTurnoService(LoteRepositoryPort loteRepository,
                               ReporteDiarioRepositoryPort reporteDiarioRepository,
                               AlertaSanitariaRepositoryPort alertaSanitariaRepository,
                               NotificadorAlertaSanitariaPort notificador,
                               Clock clock) {
        this.loteRepository = loteRepository;
        this.reporteDiarioRepository = reporteDiarioRepository;
        this.alertaSanitariaRepository = alertaSanitariaRepository;
        this.notificador = notificador;
        this.clock = clock;
    }

    @Override
    public CerrarTurnoResult cerrarTurno(CerrarTurnoCommand comando) {
        Lote lote = loteRepository.buscarPorId(comando.getLoteId())
                .orElseThrow(() -> new LoteNoEncontradoException(comando.getLoteId()));

        List<String> camposFaltantes = validarCamposObligatorios(comando);
        if (!camposFaltantes.isEmpty()) {
            return CerrarTurnoResult.camposIncompletos(camposFaltantes);
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

            return CerrarTurnoResult.bloqueadoPorAlertaSanitaria(reporte, alerta);
        }

        lote.registrarBajas(comando.getCantidadBajas());
        loteRepository.guardar(lote);

        reporte.consolidar();
        reporteDiarioRepository.guardar(reporte);

        return CerrarTurnoResult.turnoCerrado(reporte);
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
