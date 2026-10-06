package com.avimanager.domain.port.in;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.model.Turno;

import java.util.List;

/**
 * Resultado del caso de uso "Cerrar turno diario". Modela los tres desenlaces
 * posibles del flujo de negocio (ver Especificacion CU "Gestionar turno diario"):
 *   1) CAMPOS_INCOMPLETOS   -> RN-05 bloquea "Guardar y Salir"; el turno sigue ABIERTO
 *   2) ALERTA_SANITARIA     -> RN-01/RN-07 bloquea el cierre y notifica al veterinario
 *   3) TURNO_CERRADO        -> turno consolidado exitosamente
 * En los tres casos se devuelve el turno (F-10) para mostrar su estado.
 */
public class CerrarTurnoResult {

    public enum Estado {
        CAMPOS_INCOMPLETOS,
        ALERTA_SANITARIA,
        TURNO_CERRADO
    }

    private final Estado estado;
    private final List<String> camposFaltantes;
    private final ReporteDiario reporteDiario;
    private final AlertaSanitaria alertaSanitaria;
    private final Turno turno;

    private CerrarTurnoResult(Estado estado, List<String> camposFaltantes, ReporteDiario reporteDiario,
                               AlertaSanitaria alertaSanitaria, Turno turno) {
        this.estado = estado;
        this.camposFaltantes = camposFaltantes;
        this.reporteDiario = reporteDiario;
        this.alertaSanitaria = alertaSanitaria;
        this.turno = turno;
    }

    public static CerrarTurnoResult camposIncompletos(List<String> camposFaltantes, Turno turno) {
        return new CerrarTurnoResult(Estado.CAMPOS_INCOMPLETOS, camposFaltantes, null, null, turno);
    }

    public static CerrarTurnoResult bloqueadoPorAlertaSanitaria(ReporteDiario reporteDiario, AlertaSanitaria alerta,
                                                                Turno turno) {
        return new CerrarTurnoResult(Estado.ALERTA_SANITARIA, List.of(), reporteDiario, alerta, turno);
    }

    public static CerrarTurnoResult turnoCerrado(ReporteDiario reporteDiario, Turno turno) {
        return new CerrarTurnoResult(Estado.TURNO_CERRADO, List.of(), reporteDiario, null, turno);
    }

    public Estado getEstado() {
        return estado;
    }

    public List<String> getCamposFaltantes() {
        return camposFaltantes;
    }

    public ReporteDiario getReporteDiario() {
        return reporteDiario;
    }

    public AlertaSanitaria getAlertaSanitaria() {
        return alertaSanitaria;
    }

    public Turno getTurno() {
        return turno;
    }
}
