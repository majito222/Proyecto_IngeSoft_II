import { apiFetch } from "./cliente";

export function cerrarTurno(loteId, payload) {
  return apiFetch(`/lotes/${encodeURIComponent(loteId)}/turnos/cierre`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
