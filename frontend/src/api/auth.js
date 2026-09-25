import { apiFetch } from "./cliente";

export function iniciarSesion(username, contrasena) {
  return apiFetch("/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, contrasena }),
  });
}
