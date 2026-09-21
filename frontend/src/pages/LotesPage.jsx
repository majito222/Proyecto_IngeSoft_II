import { IconLotes } from "../components/icons";
import PaginaProxima from "./PaginaProxima";

export default function LotesPage() {
  return (
    <PaginaProxima
      icono={IconLotes}
      titulo="Lotes y galpones"
      descripcion="Consulta y administra los galpones y lotes activos."
      funcionalidades={["F-06 — Registrar y consultar galpones", "F-07 — Consultar historial de lotes"]}
    />
  );
}
