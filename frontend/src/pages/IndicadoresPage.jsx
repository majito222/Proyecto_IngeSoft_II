import { useCallback, useEffect, useState } from "react";
import { consolidarReporte, listarConsolidados, listarKpis } from "../api/kpis";
import { useSesion } from "../auth/SesionContext";
import { formatearFecha, formatearHora } from "../components/EstadoTurno";

// Orden de las columnas; la clave es la que devuelve cada CalculadoraKpi del backend.
const COLUMNAS = [
  { clave: "CONVERSION_ALIMENTICIA", titulo: "Conversión alimenticia", sufijo: "" },
  { clave: "MORTALIDAD_ACUMULADA", titulo: "Mortalidad acumulada", sufijo: "%" },
  { clave: "PRODUCCION_TOTAL", titulo: "Producción del día", sufijo: " bandejas" },
];

function hoyIso() {
  return new Date().toLocaleDateString("en-CA"); // AAAA-MM-DD en hora local
}

function kpi(lote, clave) {
  return lote.kpis.find((k) => k.clave === clave);
}

function formatear(numero, decimales = 2) {
  return numero.toLocaleString("es-CO", { maximumFractionDigits: decimales });
}

function ValorKpi({ dato, sufijo }) {
  if (!dato) return "—";
  if (dato.valor == null) {
    return <span className="kpi__sin-dato" title={dato.detalle ?? undefined}>Sin datos</span>;
  }
  return (
    <span title={dato.detalle ?? undefined}>
      {formatear(dato.valor)}
      {sufijo}
      {dato.estimado && <span className="kpi__estimado">estimado</span>}
    </span>
  );
}

function TablaKpis({ lotes }) {
  const produccionTotal = lotes.reduce((total, l) => total + (kpi(l, "PRODUCCION_TOTAL")?.valor ?? 0), 0);
  return (
    <div className="tabla-turnos">
      <table>
        <thead>
          <tr>
            <th>Galpón</th>
            <th className="numero">Aves vivas</th>
            {COLUMNAS.map((c) => (
              <th key={c.clave} className="numero">
                {c.titulo}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {lotes.map((lote) => (
            <tr key={lote.loteId}>
              <td>
                {lote.galponNombre}
                <small>{lote.loteId}</small>
              </td>
              <td className="numero">{formatear(lote.avesVivas, 0)}</td>
              {COLUMNAS.map((c) => (
                <td key={c.clave} className="numero">
                  <ValorKpi dato={kpi(lote, c.clave)} sufijo={c.sufijo} />
                  {kpi(lote, c.clave)?.valor == null && kpi(lote, c.clave)?.detalle && (
                    <small>{kpi(lote, c.clave).detalle}</small>
                  )}
                </td>
              ))}
            </tr>
          ))}
          {lotes.length === 0 && (
            <tr>
              <td colSpan={2 + COLUMNAS.length} className="tabla-turnos__vacia">
                No hay lotes activos.
              </td>
            </tr>
          )}
        </tbody>
        {lotes.length > 0 && (
          <tfoot>
            <tr>
              <td colSpan={2 + COLUMNAS.length - 1}>Producción total de la granja</td>
              <td className="numero">{formatear(produccionTotal, 0)} bandejas</td>
            </tr>
          </tfoot>
        )}
      </table>
    </div>
  );
}

/**
 * F-04.2: dashboard de KPIs diarios de todos los galpones (Dueño y Administrador).
 * F-04.1: el Administrador consolida el reporte del día; se rechaza si queda algún
 * turno de esa fecha sin aprobar (F-04.3, se aprueban en el Panel de turnos).
 */
export default function IndicadoresPage() {
  const { tienePermiso } = useSesion();
  const puedeConsolidar = tienePermiso("APROBAR_REPORTES");
  const [fecha, setFecha] = useState(hoyIso);
  const [respuesta, setRespuesta] = useState(null); // { lotes, consolidados } | { error }
  const [consolidando, setConsolidando] = useState(false);
  const [aviso, setAviso] = useState(null); // { tipo: "exito" | "error", texto }

  const cargar = useCallback(async () => {
    setRespuesta(null);
    try {
      const [kpis, consolidados] = await Promise.all([listarKpis(fecha), listarConsolidados()]);
      if (kpis.status !== 200) {
        setRespuesta({ error: kpis.data?.mensaje ?? `Respuesta inesperada del servidor (HTTP ${kpis.status})` });
      } else {
        setRespuesta({ lotes: kpis.data, consolidados: consolidados.status === 200 ? consolidados.data : [] });
      }
    } catch {
      setRespuesta({ error: "No se pudo conectar con el servidor. Verifica que el backend esté corriendo." });
    }
  }, [fecha]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  async function consolidar() {
    setConsolidando(true);
    setAviso(null);
    try {
      const { status, data } = await consolidarReporte(fecha);
      if (status === 201) {
        setAviso({ tipo: "exito", texto: `Reporte del ${formatearFecha(fecha)} consolidado.` });
        cargar();
      } else {
        setAviso({ tipo: "error", texto: data?.mensaje ?? `No se pudo consolidar (HTTP ${status})` });
      }
    } catch {
      setAviso({ tipo: "error", texto: "No se pudo conectar con el servidor." });
    } finally {
      setConsolidando(false);
    }
  }

  const lotes = respuesta?.lotes ?? [];
  const consolidados = respuesta?.consolidados ?? [];
  const yaConsolidado = consolidados.some((r) => r.fecha === fecha);

  return (
    <div className="pagina pagina--ancha">
      <header className="pagina__encabezado">
        <h1>Indicadores</h1>
        <p>KPIs diarios de todos los galpones. Pasa el cursor sobre un valor para ver cómo se calculó.</p>
      </header>

      <div className="tarjeta barra-filtros">
        <label htmlFor="kpi-fecha">Fecha</label>
        <input id="kpi-fecha" type="date" value={fecha} max={hoyIso()} onChange={(e) => setFecha(e.target.value)} />
        <button type="button" className="boton-secundario" onClick={() => setFecha(hoyIso())}>
          Hoy
        </button>
        {puedeConsolidar && (
          <button
            type="button"
            className="boton-secundario boton-secundario--principal barra-filtros__actualizar"
            onClick={consolidar}
            disabled={consolidando || yaConsolidado || !respuesta?.lotes}
          >
            {yaConsolidado ? "Día ya consolidado" : consolidando ? "Consolidando…" : "Consolidar reporte del día"}
          </button>
        )}
      </div>

      {aviso && (
        <div className={`tarjeta resultado resultado--${aviso.tipo}`} role="status">
          <p>{aviso.texto}</p>
          {aviso.tipo === "error" && puedeConsolidar && (
            <p className="ayuda">Aprueba los reportes pendientes en el Panel de turnos y vuelve a intentarlo.</p>
          )}
        </div>
      )}

      {!respuesta && <div className="tarjeta">Calculando indicadores…</div>}

      {respuesta?.error && (
        <div className="tarjeta resultado resultado--error">
          <h3>No se pudieron cargar los indicadores</h3>
          <p>{respuesta.error}</p>
        </div>
      )}

      {respuesta?.lotes && <TablaKpis lotes={lotes} />}

      {respuesta?.lotes && (
        <section className="consolidados">
          <h2>Reportes consolidados</h2>
          {consolidados.length === 0 && (
            <p className="ayuda">Todavía no se ha consolidado ningún día.</p>
          )}
          {consolidados.map((reporte) => (
            <details key={reporte.id} className="tarjeta consolidado">
              <summary>
                <strong>{formatearFecha(reporte.fecha)}</strong>
                <span>
                  Consolidado por {reporte.consolidadoPorNombre} el {formatearFecha(reporte.fechaHora)} a las{" "}
                  {formatearHora(reporte.fechaHora)} · {reporte.turnosAprobados} turno
                  {reporte.turnosAprobados === 1 ? "" : "s"} aprobado{reporte.turnosAprobados === 1 ? "" : "s"}
                </span>
              </summary>
              <TablaKpis lotes={reporte.lotes} />
            </details>
          ))}
        </section>
      )}
    </div>
  );
}
