import { apiFetch } from "./cliente";

export function cerrarTurno(loteId, payload) {
  return apiFetch(`/lotes/${encodeURIComponent(loteId)}/turnos/cierre`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

// F-10.2: galpón asignado del worker y su turno de hoy.
export function consultarTurnoActual() {
  return apiFetch("/turnos/actual");
}

export function iniciarTurno() {
  return apiFetch("/turnos/inicio", { method: "POST" });
}

// F-10.3: dashboard de cierres pendientes (solo Administrador).
export function listarTurnos({ fecha } = {}) {
  const params = new URLSearchParams();
  if (fecha) params.set("fecha", fecha);
  const query = params.toString();
  return apiFetch(`/turnos${query ? `?${query}` : ""}`);
}
