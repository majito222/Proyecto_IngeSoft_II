import { apiFetch } from "./cliente";

// F-11.2: galpones con su lote activo y sus workers (roles con VER_LOTES).
export function listarGalpones() {
  return apiFetch("/galpones");
}

// F-11.1: el Administrador registra un galpón nuevo.
export function registrarGalpon({ nombre, capacidadMaxima }) {
  return apiFetch("/galpones", {
    method: "POST",
    body: JSON.stringify({ nombre, capacidadMaxima }),
  });
}

// F-11.1: aloja un lote en un galpón libre (respeta capacidad y lote activo único).
export function registrarLote(galponId, { poblacionInicial, edadSemanas }) {
  return apiFetch(`/galpones/${encodeURIComponent(galponId)}/lotes`, {
    method: "POST",
    body: JSON.stringify({ poblacionInicial, edadSemanas }),
  });
}

// F-11.1: el lote sale del galpón, que queda libre para otro.
export function finalizarLote(loteId) {
  return apiFetch(`/lotes/${encodeURIComponent(loteId)}/finalizacion`, { method: "POST" });
}

// F-11.1: operarios que se pueden asignar a un galpón.
export function listarWorkers() {
  return apiFetch("/workers");
}

// F-11.1: asigna un worker a un galpón; galponId null lo deja sin galpón.
export function asignarGalpon(username, galponId) {
  return apiFetch(`/usuarios/${encodeURIComponent(username)}/galpon`, {
    method: "PUT",
    body: JSON.stringify({ galponId }),
  });
}
