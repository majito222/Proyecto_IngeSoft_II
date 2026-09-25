import { NavLink } from "react-router-dom";
import { useSesion } from "../auth/SesionContext";
import { IconSalir } from "../components/icons";
import { ITEMS_MENU } from "./menu";

export default function Sidebar({ onNavigate }) {
  const { usuario, tienePermiso, cerrarSesion } = useSesion();
  const items = ITEMS_MENU.filter((item) => tienePermiso(item.permiso));

  return (
    <nav className="sidebar">
      <div className="sidebar__marca">
        <span className="sidebar__logo">🐔</span>
        <div>
          <strong>AviManager</strong>
          <small>Gestión de galpones</small>
        </div>
      </div>

      <ul className="sidebar__lista">
        {items.map(({ to, label, icon: Icon }) => (
          <li key={to}>
            <NavLink
              to={to}
              onClick={onNavigate}
              className={({ isActive }) => "sidebar__link" + (isActive ? " sidebar__link--activo" : "")}
            >
              <Icon />
              {label}
            </NavLink>
          </li>
        ))}
      </ul>

      <div className="sidebar__usuario">
        <div>
          <strong>{usuario.nombreCompleto}</strong>
          <small>{usuario.rolDescripcion}</small>
        </div>
        <button type="button" className="sidebar__salir" onClick={cerrarSesion} aria-label="Cerrar sesión" title="Cerrar sesión">
          <IconSalir />
        </button>
      </div>
    </nav>
  );
}
