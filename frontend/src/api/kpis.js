import { apiFetch } from "./cliente";

// F-04.2: KPIs del día de cada lote activo (Dueño y Administrador).
export function listarKpis(fecha) {
  return apiFetch(`/kpis${fecha ? `?fecha=${encodeURIComponent(fecha)}` : ""}`);
}

// F-04.1: reportes diarios ya consolidados.
export function listarConsolidados() {
  return apiFetch("/kpis/consolidados");
}

// F-04.1: el Administrador consolida el reporte del día (exige todos los turnos aprobados).
export function consolidarReporte(fecha) {
  return apiFetch("/kpis/consolidacion", { method: "POST", body: JSON.stringify({ fecha }) });
}
