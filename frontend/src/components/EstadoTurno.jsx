// Estados del turno (F-10) en el orden de su ciclo de vida.
export const ESTADOS_TURNO = {
  ABIERTO: { etiqueta: "Abierto", variante: "pendiente" },
  BLOQUEADO_ALERTA_SANITARIA: { etiqueta: "Bloqueado por alerta", variante: "peligro" },
  CERRADO: { etiqueta: "Cerrado", variante: "exito" },
  CERRADO_CON_ALERTA: { etiqueta: "Cerrado con alerta", variante: "alerta-atendida" },
  APROBADO: { etiqueta: "Aprobado", variante: "aprobado" },
};

export default function EstadoTurno({ estado }) {
  const info = ESTADOS_TURNO[estado] ?? { etiqueta: estado, variante: "pendiente" };
  return <span className={`estado-turno estado-turno--${info.variante}`}>{info.etiqueta}</span>;
}

export function formatearFecha(fechaIso) {
  if (!fechaIso) return "—";
  const [anio, mes, dia] = fechaIso.slice(0, 10).split("-");
  return `${dia}/${mes}/${anio}`;
}

export function formatearHora(fechaHoraIso) {
  return fechaHoraIso ? fechaHoraIso.slice(11, 16) : "—";
}
