import { Navigate } from "react-router-dom";
import { useSesion } from "../auth/SesionContext";
import { ITEMS_MENU } from "../layout/menu";

// Lleva a cada usuario a la primera sección que su rol puede ver.
export default function InicioPage() {
  const { tienePermiso } = useSesion();
  const primera = ITEMS_MENU.find((item) => tienePermiso(item.permiso));

  if (primera) return <Navigate to={primera.to} replace />;

  return (
    <div className="pagina">
      <div className="tarjeta">
        <h3>Sin secciones disponibles</h3>
        <p>Tu rol todavía no tiene secciones habilitadas en esta versión.</p>
      </div>
    </div>
  );
}
