const BASE_URL = "/api";

export async function cerrarTurno(loteId, payload) {
  const response = await fetch(`${BASE_URL}/lotes/${encodeURIComponent(loteId)}/turnos/cierre`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  const data = await response.json().catch(() => null);
  return { status: response.status, data };
}
