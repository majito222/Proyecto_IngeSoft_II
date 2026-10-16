package com.avimanager.domain.service.kpi;

import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.model.ResultadoKpi;
import com.avimanager.domain.policy.PoliticaPeso;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Estrategia de KPI (patron Strategy): conversion alimenticia de la ultima
 * semana pesada = kg de alimento consumidos / kg de peso ganados.
 *
 * Acordado con el bloque A (pesaje semanal, F-07):
 *   kg ganados = (peso del ultimo pesaje - peso del pesaje anterior) x aves vivas / 1000
 *   alimento   = consumo de los cierres de turno despues del pesaje anterior y hasta el ultimo
 * Si el lote tiene un solo pesaje, el "anterior" es el peso estandar de la semana
 * previa (PoliticaPeso) y el KPI queda marcado como estimado. Sin pesajes, o sin
 * consumo o ganancia en el periodo, no hay valor.
 */
public class ConversionAlimenticiaKpi implements CalculadoraKpi {

    public static final String CLAVE = "CONVERSION_ALIMENTICIA";
    private static final String NOMBRE = "Conversión alimenticia";
    private static final String UNIDAD = "kg alimento / kg ganado";

    @Override
    public ResultadoKpi calcular(DatosKpiLote datos) {
        List<Pesaje> pesajes = datos.getPesajes().stream()
                .filter(p -> !p.getFecha().isAfter(datos.getFecha()))
                .sorted(Comparator.comparing(Pesaje::getFechaHoraRegistro))
                .toList();
        if (pesajes.isEmpty()) {
            return sinValor("El lote aún no tiene pesajes");
        }

        Pesaje ultimo = pesajes.get(pesajes.size() - 1);
        boolean estimado = pesajes.size() == 1;
        double pesoAnteriorGramos;
        LocalDate desde;
        if (estimado) {
            int semanaAnterior = Math.max(PoliticaPeso.EDAD_MINIMA_SEMANAS, ultimo.getEdadSemanas() - 1);
            pesoAnteriorGramos = PoliticaPeso.pesoEstandarGramos(semanaAnterior);
            desde = ultimo.getFecha().minusDays(7);
        } else {
            Pesaje anterior = pesajes.get(pesajes.size() - 2);
            pesoAnteriorGramos = anterior.getPesoPromedioGramos();
            desde = anterior.getFecha();
        }

        Lote lote = datos.getLote();
        double kgGanados = (ultimo.getPesoPromedioGramos() - pesoAnteriorGramos) * lote.getPoblacionActual() / 1000.0;
        double kgAlimento = datos.getReportes().stream()
                .filter(r -> r.getFecha().isAfter(desde) && !r.getFecha().isAfter(ultimo.getFecha()))
                .filter(r -> r.getConsumoAlimentoKg() != null)
                .mapToDouble(ReporteDiario::getConsumoAlimentoKg)
                .sum();

        if (kgGanados <= 0) {
            return sinValor("Sin ganancia de peso entre pesajes");
        }
        if (kgAlimento <= 0) {
            return sinValor("Sin consumo de alimento registrado entre pesajes");
        }
        String detalle = String.format("%.1f kg de alimento / %.1f kg ganados (%s a %s)%s",
                kgAlimento, kgGanados, desde, ultimo.getFecha(), estimado ? ", peso anterior estándar" : "");
        return new ResultadoKpi(CLAVE, NOMBRE, UNIDAD, CalculadoraKpi.redondear(kgAlimento / kgGanados), estimado,
                detalle);
    }

    private ResultadoKpi sinValor(String motivo) {
        return new ResultadoKpi(CLAVE, NOMBRE, UNIDAD, null, false, motivo);
    }
}
