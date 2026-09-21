import { NavLink } from "react-router-dom";
import { IconAlertas, IconCierre, IconLotes } from "../components/icons";

const ITEMS = [
  { to: "/", label: "Cierre de turno", icon: IconCierre, end: true },
  { to: "/lotes", label: "Lotes y galpones", icon: IconLotes },
  { to: "/alertas", label: "Alertas sanitarias", icon: IconAlertas },
];

export default function Sidebar({ onNavigate }) {
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
        {ITEMS.map(({ to, label, icon: Icon, end }) => (
          <li key={to}>
            <NavLink
              to={to}
              end={end}
              onClick={onNavigate}
              className={({ isActive }) => "sidebar__link" + (isActive ? " sidebar__link--activo" : "")}
            >
              <Icon />
              {label}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  );
}
