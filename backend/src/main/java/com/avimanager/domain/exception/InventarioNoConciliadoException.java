package com.avimanager.domain.exception;

import com.avimanager.domain.model.DiferenciaInventario;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * RN-06: el conteo fisico de la bodega no cuadra con el sistema, asi que el dia
 * no se puede consolidar hasta registrar un Acta de Ajuste (F-05).
 */
public class InventarioNoConciliadoException extends RuntimeException {

    private final transient List<DiferenciaInventario> diferencias;

    public InventarioNoConciliadoException(LocalDate fecha, List<DiferenciaInventario> diferencias) {
        super("No se puede consolidar el " + fecha + ": el conteo físico no cuadra con el sistema ("
                + diferencias.stream().map(InventarioNoConciliadoException::describir).collect(Collectors.joining("; "))
                + "). Registra un Acta de Ajuste antes de consolidar.");
        this.diferencias = List.copyOf(diferencias);
    }

    private static String describir(DiferenciaInventario d) {
        return d.getInsumoNombre() + ": sistema " + cantidad(d.getStockSistema()) + " " + d.getUnidad()
                + ", contado " + cantidad(d.getStockContado()) + " " + d.getUnidad();
    }

    /** 5005.0 -> "5.005"; 12.5 -> "12,5" (formato colombiano, sin decimales sobrantes). */
    private static String cantidad(double valor) {
        DecimalFormat formato = new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO")));
        return formato.format(valor);
    }

    public List<DiferenciaInventario> getDiferencias() {
        return diferencias;
    }
}
