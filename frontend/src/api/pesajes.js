import { apiFetch } from "./cliente";

// F-07.1: galpón asignado, peso estándar de la semana y pesaje de esta semana (si ya lo hay).
export function consultarReferenciaPesaje() {
  return apiFetch("/pesajes/referencia");
}

// F-07.1: el worker registra el peso promedio de la muestra de su galpón.
export function registrarPesaje({ pesoPromedioGramos, tamanoMuestra }) {
  return apiFetch("/pesajes", {
    method: "POST",
    body: JSON.stringify({ pesoPromedioGramos, tamanoMuestra }),
  });
}

// F-07.2: el zootecnista lista los pesajes (pendientes primero).
export function listarPesajes() {
  return apiFetch("/pesajes");
}

// RN-02: el zootecnista aprueba el ajuste de ración o mantiene la estándar.
export function revisarPesaje(pesajeId, aprobarAjuste) {
  return apiFetch(`/pesajes/${encodeURIComponent(pesajeId)}/revision`, {
    method: "POST",
    body: JSON.stringify({ aprobarAjuste }),
  });
}
