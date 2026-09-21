import { IconAlertas } from "../components/icons";
import PaginaProxima from "./PaginaProxima";

export default function AlertasPage() {
  return (
    <PaginaProxima
      icono={IconAlertas}
      titulo="Alertas sanitarias"
      descripcion="Historial de alertas generadas por mortalidad crítica y su seguimiento."
      funcionalidades={["F-08 — Consultar historial de alertas sanitarias", "Seguimiento del estado de lotes en observación"]}
    />
  );
}
