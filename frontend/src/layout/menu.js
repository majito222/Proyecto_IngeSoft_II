import {
  IconAlertas,
  IconCierre,
  IconDano,
  IconInventario,
  IconKpis,
  IconLotes,
  IconOrdenes,
  IconPesaje,
  IconRacion,
  IconRevisionPesajes,
  IconTurnos,
} from "../components/icons";

// Cada opción del menú exige un permiso; solo se muestran las que tiene el rol (F-09).
// El orden importa: cada usuario entra a la primera opción que puede ver.
export const ITEMS_MENU = [
  { to: "/turnos", label: "Panel de turnos", icon: IconTurnos, permiso: "VER_TURNOS" },
  { to: "/indicadores", label: "Indicadores", icon: IconKpis, permiso: "VER_DASHBOARD_KPI" },
  { to: "/cierre-turno", label: "Cierre de turno", icon: IconCierre, permiso: "CERRAR_TURNO" },
  { to: "/pesaje", label: "Pesaje semanal", icon: IconPesaje, permiso: "REGISTRAR_PESO" },
  { to: "/reportar-dano", label: "Reportar daño", icon: IconDano, permiso: "REPORTAR_DANOS" },
  { to: "/pesajes", label: "Revisión de pesajes", icon: IconRevisionPesajes, permiso: "REVISAR_PESAJES" },
  { to: "/ordenes", label: "Órdenes de mantenimiento", icon: IconOrdenes, permiso: "ATENDER_ORDENES_MANTENIMIENTO" },
  { to: "/racion", label: "Ración diaria", icon: IconRacion, permiso: "VER_RACION" },
  { to: "/lotes", label: "Lotes y galpones", icon: IconLotes, permiso: "VER_LOTES" },
  { to: "/inventario", label: "Inventario", icon: IconInventario, permiso: "VER_INVENTARIO" },
  { to: "/alertas", label: "Alertas sanitarias", icon: IconAlertas, permiso: "VER_ALERTAS_SANITARIAS" },
];
