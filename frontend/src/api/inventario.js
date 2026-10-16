import { apiFetch } from "./cliente";

// F-03.2: insumos de la bodega con su stock y % disponible (Administrador y Dueño).
export function listarInventario() {
  return apiFetch("/inventario");
}
