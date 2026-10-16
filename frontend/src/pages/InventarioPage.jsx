import { useCallback, useEffect, useState } from "react";
import { listarActasAjuste, listarInventario, registrarActaAjuste } from "../api/inventario";
import { useSesion } from "../auth/SesionContext";
import { formatearFecha, formatearHora } from "../components/EstadoTurno";

const TIPOS_INSUMO = {
  ALIMENTO: "Alimento",
  SANIDAD: "Sanidad",
  MATERIAL_CAMA: "Material de cama",
};

const UMBRAL_CRITICO = 50;
const LONGITUD_MINIMA_JUSTIFICACION = 10;

function formatear(numero) {
  return numero.toLocaleString("es-CO", { maximumFractionDigits: 1 });
}

function formatearDiferencia(numero) {
  if (numero === 0) return "0";
  return `${numero > 0 ? "+" : "−"}${formatear(Math.abs(numero))}`;
}

/**
 * F-03.2: la bodega con el stock, la capacidad y el % disponible de cada
 * insumo. Bajo el 50% el insumo queda en "Stock Crítico" (RN-03). El cierre de
 * turno descuenta automáticamente el alimento consumido.
 * F-05.2: solo el Administrador (AJUSTAR_INVENTARIO) registra Actas de Ajuste;
 * el historial de actas lo ven todos los que ven el inventario.
 */
export default function InventarioPage() {
  const { tienePermiso } = useSesion();
  const puedeAjustar = tienePermiso("AJUSTAR_INVENTARIO");
  const [respuesta, setRespuesta] = useState(null); // { insumos, actas } | { error }

  const cargar = useCallback(async () => {
    try {
      const [inventario, actas] = await Promise.all([listarInventario(), listarActasAjuste()]);
      if (inventario.status !== 200) {
        setRespuesta({ error: inventario.data?.mensaje ?? `Respuesta inesperada del servidor (HTTP ${inventario.status})` });
      } else {
        setRespuesta({ insumos: inventario.data, actas: actas.status === 200 ? actas.data : [] });
      }
    } catch {
      setRespuesta({ error: "No se pudo conectar con el servidor. Verifica que el backend esté corriendo." });
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const insumos = respuesta?.insumos ?? [];
  const actas = respuesta?.actas ?? [];
  const criticos = insumos.filter((i) => i.critico).length;

  return (
    <div className="pagina pagina--ancha">
      <header className="pagina__encabezado">
        <h1>Inventario</h1>
        <p>
          Stock de la bodega. El cierre de cada turno descuenta el alimento consumido, y un insumo por debajo del{" "}
          {UMBRAL_CRITICO}% de su capacidad queda en Stock Crítico.
        </p>
      </header>

      {respuesta?.insumos && (
        <div className="filtro-estados">
          <div className="contador">
            <span className="contador__numero">{insumos.length}</span>
            <span className="contador__etiqueta">Insumos en bodega</span>
          </div>
          <div className={`contador ${criticos > 0 ? "contador--peligro" : "contador--exito"}`}>
            <span className="contador__numero">{criticos}</span>
            <span className="contador__etiqueta">En Stock Crítico</span>
          </div>
          <div className="barra-filtros__actualizar inventario__actualizar">
            <button type="button" className="boton-secundario" onClick={cargar}>
              Actualizar
            </button>
          </div>
        </div>
      )}

      {!respuesta && <div className="tarjeta">Cargando inventario…</div>}

      {respuesta?.error && (
        <div className="tarjeta resultado resultado--error">
          <h3>No se pudo cargar el inventario</h3>
          <p>{respuesta.error}</p>
        </div>
      )}

      {respuesta?.insumos?.length === 0 && (
        <div className="tarjeta">
          <p>La bodega no tiene insumos registrados.</p>
        </div>
      )}

      <div className="inventario">
        {insumos.map((insumo) => (
          <TarjetaInsumo key={insumo.id} insumo={insumo} puedeAjustar={puedeAjustar} onAjustado={cargar} />
        ))}
      </div>

      {respuesta?.insumos && <HistorialActas actas={actas} />}
    </div>
  );
}

function TarjetaInsumo({ insumo, puedeAjustar, onAjustado }) {
  const porcentaje = Math.min(insumo.porcentajeDisponible, 100);

  return (
    <article className={`tarjeta insumo ${insumo.critico ? "insumo--critico" : ""}`}>
      <div className="insumo__cabecera">
        <div>
          <h3 className="insumo__nombre">{insumo.nombre}</h3>
          <span className="insumo__tipo">{TIPOS_INSUMO[insumo.tipo] ?? insumo.tipo}</span>
        </div>
        <span className={`estado-turno estado-turno--${insumo.critico ? "peligro" : "exito"}`}>
          {insumo.critico ? "Stock Crítico" : "Disponible"}
        </span>
      </div>

      <div className="insumo__porcentaje">
        {formatear(insumo.porcentajeDisponible)}%<small> disponible</small>
      </div>

      <div
        className="insumo__barra"
        role="meter"
        aria-label={`${insumo.nombre}: ${formatear(insumo.porcentajeDisponible)}% disponible`}
        aria-valuemin="0"
        aria-valuemax="100"
        aria-valuenow={Math.round(porcentaje)}
      >
        <span className="insumo__relleno" style={{ width: `${porcentaje}%` }} />
        <span className="insumo__umbral" style={{ left: `${UMBRAL_CRITICO}%` }} title="Umbral de Stock Crítico (50%)" />
      </div>

      <dl className="insumo__datos">
        <div>
          <dt>Stock</dt>
          <dd>
            {formatear(insumo.stock)} {insumo.unidad}
          </dd>
        </div>
        <div>
          <dt>Capacidad</dt>
          <dd>
            {formatear(insumo.capacidad)} {insumo.unidad}
          </dd>
        </div>
      </dl>

      {insumo.critico && (
        <p className="insumo__aviso">Por debajo del {UMBRAL_CRITICO}% de la capacidad: hay que reabastecer.</p>
      )}

      {puedeAjustar && <FormularioActa insumo={insumo} onAjustado={onAjustado} />}
    </article>
  );
}

function FormularioActa({ insumo, onAjustado }) {
  const [abierto, setAbierto] = useState(false);
  const [stockContado, setStockContado] = useState("");
  const [justificacion, setJustificacion] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  const contado = stockContado === "" ? null : Number(stockContado);
  const diferencia = contado == null ? null : Math.round((contado - insumo.stock) * 10) / 10;

  function cerrar() {
    setAbierto(false);
    setStockContado("");
    setJustificacion("");
    setError(null);
  }

  async function manejarEnvio(evento) {
    evento.preventDefault();
    setEnviando(true);
    setError(null);
    try {
      const { status, data } = await registrarActaAjuste(insumo.id, { stockContado: contado, justificacion });
      if (status === 201) {
        cerrar();
        onAjustado();
      } else {
        setError(data?.mensaje ?? `No se pudo registrar el acta (HTTP ${status})`);
      }
    } catch {
      setError("No se pudo conectar con el servidor.");
    } finally {
      setEnviando(false);
    }
  }

  if (!abierto) {
    return (
      <button type="button" className="boton-secundario" onClick={() => setAbierto(true)}>
        Registrar acta de ajuste
      </button>
    );
  }

  return (
    <form className="acta" onSubmit={manejarEnvio}>
      <h4>Acta de ajuste</h4>
      <div className="campo">
        <label htmlFor={`contado-${insumo.id}`}>Stock contado en bodega ({insumo.unidad})</label>
        <input
          id={`contado-${insumo.id}`}
          type="number"
          min="0"
          max={insumo.capacidad}
          step="0.1"
          required
          value={stockContado}
          onChange={(e) => setStockContado(e.target.value)}
        />
        {diferencia != null && (
          <span className={`acta__diferencia ${diferencia < 0 ? "acta__diferencia--faltante" : ""}`}>
            Diferencia con el sistema: {formatearDiferencia(diferencia)} {insumo.unidad}
            {diferencia < 0 ? " (faltante)" : diferencia > 0 ? " (sobrante)" : " (sin diferencia)"}
          </span>
        )}
      </div>
      <div className="campo">
        <label htmlFor={`justificacion-${insumo.id}`}>Justificación</label>
        <textarea
          id={`justificacion-${insumo.id}`}
          rows="2"
          required
          minLength={LONGITUD_MINIMA_JUSTIFICACION}
          placeholder="Ej: conteo físico semanal, bultos dañados por humedad"
          value={justificacion}
          onChange={(e) => setJustificacion(e.target.value)}
        />
      </div>
      {error && (
        <p className="login__error" role="alert">
          {error}
        </p>
      )}
      <div className="acta__acciones">
        <button type="submit" className="boton-secundario boton-secundario--principal" disabled={enviando}>
          {enviando ? "Registrando…" : "Registrar acta"}
        </button>
        <button type="button" className="boton-secundario" onClick={cerrar} disabled={enviando}>
          Cancelar
        </button>
      </div>
    </form>
  );
}

function HistorialActas({ actas }) {
  return (
    <section className="historial-actas">
      <h2>Actas de ajuste</h2>
      <p className="ayuda">Cada ajuste manual del inventario queda registrado y no se puede modificar (RN-08).</p>
      <div className="tabla-turnos">
        <table>
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Insumo</th>
              <th className="numero">Sistema</th>
              <th className="numero">Contado</th>
              <th className="numero">Diferencia</th>
              <th>Justificación</th>
              <th>Administrador</th>
            </tr>
          </thead>
          <tbody>
            {actas.map((acta) => (
              <tr key={acta.id}>
                <td>
                  {formatearFecha(acta.fecha)}
                  <small>{formatearHora(acta.fecha)}</small>
                </td>
                <td>{acta.insumoNombre}</td>
                <td className="numero">
                  {formatear(acta.stockSistema)} {acta.unidad}
                </td>
                <td className="numero">
                  {formatear(acta.stockContado)} {acta.unidad}
                </td>
                <td className={`numero ${acta.diferencia < 0 ? "acta__diferencia--faltante" : ""}`}>
                  {formatearDiferencia(acta.diferencia)}
                </td>
                <td>{acta.justificacion}</td>
                <td>{acta.administradorNombre}</td>
              </tr>
            ))}
            {actas.length === 0 && (
              <tr>
                <td colSpan="7" className="tabla-turnos__vacia">
                  Todavía no hay actas de ajuste.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
