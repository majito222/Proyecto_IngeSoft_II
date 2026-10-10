import { apiFetch } from "./cliente";

// F-06.2: ración sugerida de hoy por galpón (zootecnista, operario, administrador).
export function listarRaciones() {
  return apiFetch("/racion");
}
