import { apiFetch } from "./cliente";

// F-08.2: galpones donde el usuario puede reportar daños, con sus órdenes pendientes.
export function consultarGalponesParaReporte() {
  return apiFetch("/danos/referencia");
}

// F-08.2: reportar un daño. Sin galponId se usa el galpón asignado al worker.
export function reportarDano({ galponId, tipoDano, descripcion }) {
  return apiFetch("/danos", {
    method: "POST",
    body: JSON.stringify({ galponId, tipoDano, descripcion }),
  });
}

// F-08.3: bandeja del técnico.
export function listarOrdenes() {
  return apiFetch("/ordenes-mantenimiento");
}

export function iniciarReparacion(ordenId) {
  return apiFetch(`/ordenes-mantenimiento/${encodeURIComponent(ordenId)}/inicio`, { method: "POST" });
}

export function certificarReparacion(ordenId, notaReparacion) {
  return apiFetch(`/ordenes-mantenimiento/${encodeURIComponent(ordenId)}/certificacion`, {
    method: "POST",
    body: JSON.stringify({ notaReparacion }),
  });
}
