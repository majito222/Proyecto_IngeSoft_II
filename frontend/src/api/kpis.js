import { apiFetch } from "./cliente";

// F-04.2: KPIs del día de cada lote activo (Dueño y Administrador).
export function listarKpis(fecha) {
  return apiFetch(`/kpis${fecha ? `?fecha=${encodeURIComponent(fecha)}` : ""}`);
}

// F-04.1: reportes diarios ya consolidados.
export function listarConsolidados() {
  return apiFetch("/kpis/consolidados");
}

// F-04.1: el Administrador consolida el reporte del día. Exige todos los turnos aprobados
// y que el conteo físico de cada insumo (insumoId -> cantidad) cuadre con el sistema (RN-06).
export function consolidarReporte(fecha, conteoFisico) {
  return apiFetch("/kpis/consolidacion", { method: "POST", body: JSON.stringify({ fecha, conteoFisico }) });
}
