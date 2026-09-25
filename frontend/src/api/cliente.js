const BASE_URL = "/api";
const CLAVE_SESION = "avimanager.sesion";

let alExpirarSesion = () => {};

export function registrarAlExpirarSesion(fn) {
  alExpirarSesion = fn;
}

export function leerSesionGuardada() {
  try {
    const sesion = JSON.parse(localStorage.getItem(CLAVE_SESION));
    if (!sesion?.token || tokenExpirado(sesion.token)) return null;
    return sesion;
  } catch {
    return null;
  }
}

export function guardarSesion(sesion) {
  try {
    if (sesion) localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    else localStorage.removeItem(CLAVE_SESION);
  } catch {
    // Sin localStorage (modo privado): la sesión solo dura mientras la pestaña esté abierta.
  }
}

function tokenExpirado(token) {
  try {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    return payload.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

/**
 * fetch hacia el backend que agrega el token JWT de la sesión (F-09).
 * Si el backend responde 401, la sesión venció o es inválida: se cierra.
 */
export async function apiFetch(ruta, { headers, ...opciones } = {}) {
  const token = leerSesionGuardada()?.token;
  const response = await fetch(`${BASE_URL}${ruta}`, {
    ...opciones,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
  });

  if (response.status === 401 && token) {
    alExpirarSesion();
  }

  const data = await response.json().catch(() => null);
  return { status: response.status, data };
}
