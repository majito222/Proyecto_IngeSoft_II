import { Navigate, useLocation } from "react-router-dom";
import { useSesion } from "./SesionContext";

/**
 * Sin sesión redirige al login. Si se indica un permiso y el rol del usuario
 * no lo tiene, muestra "acceso denegado" en lugar de la página.
 */
export default function RutaProtegida({ permiso, children }) {
  const { usuario, tienePermiso } = useSesion();
  const location = useLocation();

  if (!usuario) {
    return <Navigate to="/login" replace state={{ desde: location.pathname }} />;
  }

  if (permiso && !tienePermiso(permiso)) {
    return (
      <div className="pagina">
        <div className="tarjeta resultado resultado--error">
          <h3>Acceso denegado</h3>
          <p>Tu rol ({usuario.rolDescripcion}) no tiene permiso para ver esta sección.</p>
        </div>
      </div>
    );
  }

  return children;
}
