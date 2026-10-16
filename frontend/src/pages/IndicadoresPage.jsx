import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listarInventario } from "../api/inventario";
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
 * F-04.2: dashboard de KPIs diarios de todos los galpones (Dueño y Administrador),
 * calculados a la fecha consultada; un día consolidado muestra la foto guardada.
 * F-04.1: el Administrador consolida el reporte del día. Se rechaza si queda algún
 * turno sin aprobar (F-04.3) o si el conteo físico de la bodega no cuadra con el
 * sistema (RN-06): en ese caso hay que registrar un Acta de Ajuste (F-05).
 */
export default function IndicadoresPage() {
  const { tienePermiso } = useSesion();
  const puedeConsolidar = tienePermiso("APROBAR_REPORTES");
  const [fecha, setFecha] = useState(hoyIso);
  const [respuesta, setRespuesta] = useState(null); // { lotes, consolidados } | { error }
  const [panelConteo, setPanelConteo] = useState(false);
  const [aviso, setAviso] = useState(null); // { tipo: "exito" | "error", texto, diferencias? }

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
    setPanelConteo(false);
    setAviso(null);
  }, [cargar]);

  function consolidado() {
    setPanelConteo(false);
    setAviso({ tipo: "exito", texto: `Reporte del ${formatearFecha(fecha)} consolidado.` });
    cargar();
  }

  const lotes = respuesta?.lotes ?? [];
  const consolidados = respuesta?.consolidados ?? [];
  const reporteDelDia = consolidados.find((r) => r.fecha === fecha);

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
            onClick={() => {
              setAviso(null);
              setPanelConteo(true);
            }}
            disabled={Boolean(reporteDelDia) || panelConteo || !respuesta?.lotes}
          >
            {reporteDelDia ? "Día ya consolidado" : "Consolidar reporte del día"}
          </button>
        )}
      </div>

      {panelConteo && (
        <PanelConteo
          fecha={fecha}
          onCancelar={() => setPanelConteo(false)}
          onConsolidado={consolidado}
          onRechazado={(nuevoAviso) => setAviso(nuevoAviso)}
        />
      )}

      {aviso && <AvisoConsolidacion aviso={aviso} />}

      {!respuesta && <div className="tarjeta">Calculando indicadores…</div>}

      {respuesta?.error && (
        <div className="tarjeta resultado resultado--error">
          <h3>No se pudieron cargar los indicadores</h3>
          <p>{respuesta.error}</p>
        </div>
      )}

      {reporteDelDia && respuesta?.lotes && (
        <p className="tarjeta resultado resultado--exito foto-consolidada">
          Foto del día consolidada por {reporteDelDia.consolidadoPorNombre} el{" "}
          {formatearFecha(reporteDelDia.fechaHora)} a las {formatearHora(reporteDelDia.fechaHora)}: estos valores ya
          no cambian.
        </p>
      )}

      {respuesta?.lotes && <TablaKpis lotes={lotes} />}

      {respuesta?.lotes && (
        <section className="consolidados">
          <h2>Reportes consolidados</h2>
          {consolidados.length === 0 && <p className="ayuda">Todavía no se ha consolidado ningún día.</p>}
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

/** RN-06: el Administrador ingresa el conteo físico de cada insumo antes de consolidar. */
function PanelConteo({ fecha, onCancelar, onConsolidado, onRechazado }) {
  const [insumos, setInsumos] = useState(null); // lista | { error }
  const [conteo, setConteo] = useState({});
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    listarInventario()
      .then(({ status, data }) =>
        setInsumos(status === 200 ? data : { error: data?.mensaje ?? `No se pudo cargar la bodega (HTTP ${status})` }),
      )
      .catch(() => setInsumos({ error: "No se pudo conectar con el servidor." }));
  }, []);

  async function manejarEnvio(evento) {
    evento.preventDefault();
    setEnviando(true);
    try {
      const conteoFisico = Object.fromEntries(Object.entries(conteo).map(([id, valor]) => [id, Number(valor)]));
      const { status, data } = await consolidarReporte(fecha, conteoFisico);
      if (status === 201) onConsolidado();
      else
        onRechazado({
          tipo: "error",
          texto: data?.mensaje ?? `No se pudo consolidar (HTTP ${status})`,
          diferencias: data?.diferencias,
        });
    } catch {
      onRechazado({ tipo: "error", texto: "No se pudo conectar con el servidor." });
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="tarjeta conteo-fisico" onSubmit={manejarEnvio}>
      <h3>Conteo físico de la bodega</h3>
      <p className="ayuda">
        Antes de cerrar el día, cuenta los insumos en la bodega (RN-06). Si algún conteo no cuadra con el sistema, hay
        que justificar la diferencia con un Acta de Ajuste.
      </p>
      {!insumos && <p>Cargando insumos…</p>}
      {insumos?.error && <p className="login__error">{insumos.error}</p>}
      {Array.isArray(insumos) && (
        <div className="conteo-fisico__campos">
          {insumos.map((insumo) => (
            <div className="campo" key={insumo.id}>
              <label htmlFor={`conteo-${insumo.id}`}>
                {insumo.nombre} ({insumo.unidad}) <span className="requerido">*</span>
              </label>
              <input
                id={`conteo-${insumo.id}`}
                type="number"
                min="0"
                step="any"
                required
                inputMode="decimal"
                value={conteo[insumo.id] ?? ""}
                onChange={(e) => setConteo((c) => ({ ...c, [insumo.id]: e.target.value }))}
              />
              <span className="ayuda-campo">
                En el sistema: {insumo.stock.toLocaleString("es-CO")} {insumo.unidad}
              </span>
            </div>
          ))}
        </div>
      )}
      <div className="conteo-fisico__acciones">
        <button type="button" className="boton-secundario" onClick={onCancelar} disabled={enviando}>
          Cancelar
        </button>
        <button type="submit" className="boton-primario" disabled={enviando || !Array.isArray(insumos)}>
          {enviando ? "Consolidando…" : "Consolidar reporte del día"}
        </button>
      </div>
    </form>
  );
}

function AvisoConsolidacion({ aviso }) {
  const { diferencias } = aviso;
  return (
    <div className={`tarjeta resultado resultado--${aviso.tipo}`} role="status">
      <p>{aviso.texto}</p>
      {diferencias?.length > 0 && (
        <>
          <div className="tabla-turnos">
            <table>
              <thead>
                <tr>
                  <th>Insumo</th>
                  <th className="numero">En el sistema</th>
                  <th className="numero">Contado</th>
                  <th className="numero">Diferencia</th>
                </tr>
              </thead>
              <tbody>
                {diferencias.map((d) => (
                  <tr key={d.insumoId}>
                    <td>{d.insumoNombre}</td>
                    <td className="numero">
                      {d.stockSistema.toLocaleString("es-CO")} {d.unidad}
                    </td>
                    <td className="numero">
                      {d.stockContado.toLocaleString("es-CO")} {d.unidad}
                    </td>
                    <td className="numero">
                      {d.diferencia > 0 ? "+" : ""}
                      {d.diferencia.toLocaleString("es-CO")} {d.unidad}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Link to="/inventario" className="boton-secundario conteo-fisico__acta">
            Registrar Acta de Ajuste en Inventario
          </Link>
        </>
      )}
      {aviso.tipo === "error" && !diferencias && (
        <p className="ayuda">Si quedan turnos sin aprobar, apruébalos en el Panel de turnos y vuelve a intentarlo.</p>
      )}
    </div>
  );
}
