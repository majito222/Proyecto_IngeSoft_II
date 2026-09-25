import { IconAlertas, IconCierre, IconLotes } from "../components/icons";

// Cada opción del menú exige un permiso; solo se muestran las que tiene el rol (F-09).
export const ITEMS_MENU = [
  { to: "/cierre-turno", label: "Cierre de turno", icon: IconCierre, permiso: "CERRAR_TURNO" },
  { to: "/lotes", label: "Lotes y galpones", icon: IconLotes, permiso: "VER_LOTES" },
  { to: "/alertas", label: "Alertas sanitarias", icon: IconAlertas, permiso: "VER_ALERTAS_SANITARIAS" },
];
