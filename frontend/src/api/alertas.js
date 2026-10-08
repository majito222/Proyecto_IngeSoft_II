import { apiFetch } from "./cliente";

// F-02.6: alertas sanitarias (veterinario, dueño, administrador).
export function listarAlertas() {
  return apiFetch("/alertas");
}

// F-02.5: el veterinario atiende una alerta pendiente.
export function atenderAlerta(alertaId, { diagnostico, tratamiento, liberarLote }) {
  return apiFetch(`/alertas/${encodeURIComponent(alertaId)}/atencion`, {
    method: "POST",
    body: JSON.stringify({ diagnostico, tratamiento, liberarLote }),
  });
}
