import { useCallback, useEffect, useMemo, useState } from "react";
import { iniciarSesion } from "../api/auth";
import { guardarSesion, leerSesionGuardada, registrarAlExpirarSesion } from "../api/cliente";
import { SesionContext } from "./SesionContext";

/**
 * Guarda la sesión del usuario autenticado (token JWT + datos del usuario)
 * y la expone a toda la app (F-09).
 */
export default function SesionProvider({ children }) {
  const [sesion, setSesion] = useState(leerSesionGuardada);

  const cerrarSesion = useCallback(() => {
    guardarSesion(null);
    setSesion(null);
  }, []);

  useEffect(() => {
    registrarAlExpirarSesion(cerrarSesion);
  }, [cerrarSesion]);

  const login = useCallback(async (username, contrasena) => {
    const { status, data } = await iniciarSesion(username, contrasena);
    if (status !== 200) {
      throw new Error(data?.mensaje ?? "No se pudo iniciar sesión");
    }
    guardarSesion(data);
    setSesion(data);
  }, []);

  const valor = useMemo(
    () => ({
      usuario: sesion?.usuario ?? null,
      login,
      cerrarSesion,
      tienePermiso: (permiso) => sesion?.usuario?.permisos?.includes(permiso) ?? false,
    }),
    [sesion, login, cerrarSesion],
  );

  return <SesionContext.Provider value={valor}>{children}</SesionContext.Provider>;
}
