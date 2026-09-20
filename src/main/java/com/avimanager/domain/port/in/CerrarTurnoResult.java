package com.avimanager.domain.port.in;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.ReporteDiario;

import java.util.List;

/**
 * Resultado del caso de uso "Cerrar turno diario". Modela los tres desenlaces
 * posibles del flujo de negocio (ver Especificacion CU "Gestionar turno diario"):
 *   1) CAMPOS_INCOMPLETOS   -> RN-05 bloquea "Guardar y Salir"
 *   2) ALERTA_SANITARIA     -> RN-01/RN-07 bloquea el cierre y notifica al veterinario
 *   3) TURNO_CERRADO        -> turno consolidado exitosamente
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

    private CerrarTurnoResult(Estado estado, List<String> camposFaltantes,
                               ReporteDiario reporteDiario, AlertaSanitaria alertaSanitaria) {
        this.estado = estado;
        this.camposFaltantes = camposFaltantes;
        this.reporteDiario = reporteDiario;
        this.alertaSanitaria = alertaSanitaria;
    }

    public static CerrarTurnoResult camposIncompletos(List<String> camposFaltantes) {
        return new CerrarTurnoResult(Estado.CAMPOS_INCOMPLETOS, camposFaltantes, null, null);
    }

    public static CerrarTurnoResult bloqueadoPorAlertaSanitaria(ReporteDiario reporteDiario, AlertaSanitaria alerta) {
        return new CerrarTurnoResult(Estado.ALERTA_SANITARIA, List.of(), reporteDiario, alerta);
    }

    public static CerrarTurnoResult turnoCerrado(ReporteDiario reporteDiario) {
        return new CerrarTurnoResult(Estado.TURNO_CERRADO, List.of(), reporteDiario, null);
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
}
