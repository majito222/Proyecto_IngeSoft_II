// Estados del pesaje semanal (F-07). Reusa los colores de estado-turno.
export const ESTADOS_PESAJE = {
  DENTRO_DE_RANGO: { etiqueta: "En rango", variante: "exito" },
  PENDIENTE_REVISION: { etiqueta: "Pendiente de revisión", variante: "pendiente" },
  AJUSTE_APROBADO: { etiqueta: "Ajuste aprobado", variante: "aprobado" },
  AJUSTE_DESCARTADO: { etiqueta: "Ración mantenida", variante: "alerta-atendida" },
};

// RN-02: tolerancia de ±5% frente al peso estándar.
export const TOLERANCIA_PESO = 5;

export default function EstadoPesaje({ estado }) {
  const info = ESTADOS_PESAJE[estado] ?? { etiqueta: estado, variante: "pendiente" };
  return <span className={`estado-turno estado-turno--${info.variante}`}>{info.etiqueta}</span>;
}

export function formatearDesviacion(porcentaje) {
  const signo = porcentaje > 0 ? "+" : porcentaje < 0 ? "−" : "";
  return `${signo}${Math.abs(porcentaje).toFixed(2)}%`;
}

export function formatearGramos(gramos) {
  return `${gramos.toLocaleString("es-CO", { maximumFractionDigits: 1 })} g`;
}
