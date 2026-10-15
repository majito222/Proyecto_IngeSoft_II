// Órdenes de mantenimiento (F-08). Reusa los colores de estado-turno.
export const ESTADOS_ORDEN = {
  ABIERTA: { etiqueta: "Abierta", variante: "pendiente" },
  EN_REPARACION: { etiqueta: "En reparación", variante: "aprobado" },
  CERTIFICADA: { etiqueta: "Certificada", variante: "exito" },
};

export const TIPOS_DANO = [
  { valor: "ROTURA_MALLA", etiqueta: "Rotura de malla" },
  { valor: "DANO_TECHO", etiqueta: "Daño en techo" },
  { valor: "CORTINA_DANADA", etiqueta: "Cortina dañada" },
  { valor: "OTRO", etiqueta: "Otro daño" },
];

export default function EstadoOrden({ estado }) {
  const info = ESTADOS_ORDEN[estado] ?? { etiqueta: estado, variante: "pendiente" };
  return <span className={`estado-turno estado-turno--${info.variante}`}>{info.etiqueta}</span>;
}

function duracion(minutos) {
  const h = Math.floor(minutos / 60);
  const m = minutos % 60;
  if (h === 0) return `${m} min`;
  return m === 0 ? `${h} h` : `${h} h ${m} min`;
}

// RN-04: plazo de 12 horas desde el reporte.
export function formatearPlazo(minutosRestantes) {
  return minutosRestantes < 0
    ? `Vencida hace ${duracion(-minutosRestantes)}`
    : `Quedan ${duracion(minutosRestantes)}`;
}
