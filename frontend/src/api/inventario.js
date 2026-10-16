import { apiFetch } from "./cliente";

// F-03.2: insumos de la bodega con su stock y % disponible (Administrador y Dueño).
export function listarInventario() {
  return apiFetch("/inventario");
}

// F-05.1: el Administrador registra un Acta de Ajuste con el stock contado en bodega.
export function registrarActaAjuste(insumoId, { stockContado, justificacion }) {
  return apiFetch(`/inventario/${encodeURIComponent(insumoId)}/ajustes`, {
    method: "POST",
    body: JSON.stringify({ stockContado, justificacion }),
  });
}

// F-05: historial de actas de ajuste (Administrador y Dueño).
export function listarActasAjuste() {
  return apiFetch("/inventario/ajustes");
}
