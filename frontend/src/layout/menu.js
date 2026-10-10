import { IconAlertas, IconCierre, IconLotes, IconRacion, IconTurnos } from "../components/icons";

// Cada opción del menú exige un permiso; solo se muestran las que tiene el rol (F-09).
// El orden importa: cada usuario entra a la primera opción que puede ver.
export const ITEMS_MENU = [
  { to: "/turnos", label: "Panel de turnos", icon: IconTurnos, permiso: "VER_TURNOS" },
  { to: "/cierre-turno", label: "Cierre de turno", icon: IconCierre, permiso: "CERRAR_TURNO" },
  { to: "/racion", label: "Ración diaria", icon: IconRacion, permiso: "VER_RACION" },
  { to: "/lotes", label: "Lotes y galpones", icon: IconLotes, permiso: "VER_LOTES" },
  { to: "/alertas", label: "Alertas sanitarias", icon: IconAlertas, permiso: "VER_ALERTAS_SANITARIAS" },
];
