import { useCallback, useEffect, useMemo, useState } from "react";
import { listarTurnos } from "../api/turnos";
import EstadoTurno, { ESTADOS_TURNO, formatearFecha, formatearHora } from "../components/EstadoTurno";

function hoyIso() {
  return new Date().toLocaleDateString("en-CA"); // AAAA-MM-DD en hora local
}

/**
 * F-10.3: dashboard de cierres pendientes. El administrador ve todos los
 * turnos y sus estados, y detecta qué workers no han cerrado su turno.
 */
export default function TurnosPage() {
  const [fecha, setFecha] = useState("");
  const [estado, setEstado] = useState("");
  const [respuesta, setRespuesta] = useState(null); // { turnos } | { error }

  const cargar = useCallback(async () => {
    setRespuesta(null);
    try {
      const { status, data } = await listarTurnos({ fecha });
      if (status === 200) setRespuesta({ turnos: data });
      else setRespuesta({ error: data?.mensaje ?? `Respuesta inesperada del servidor (HTTP ${status})` });
    } catch {
      setRespuesta({ error: "No se pudo conectar con el servidor. Verifica que el backend esté corriendo." });
    }
  }, [fecha]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const turnos = respuesta?.turnos ?? [];
  const conteo = useMemo(() => {
    const c = Object.fromEntries(Object.keys(ESTADOS_TURNO).map((e) => [e, 0]));
    turnos.forEach((t) => (c[t.estado] = (c[t.estado] ?? 0) + 1));
    return c;
  }, [turnos]);
  const visibles = estado ? turnos.filter((t) => t.estado === estado) : turnos;

  return (
    <div className="pagina pagina--ancha">
      <header className="pagina__encabezado">
        <h1>Panel de turnos</h1>
        <p>Todos los turnos de los workers y su estado. Los abiertos son cierres pendientes.</p>
      </header>

      <div className="filtro-estados" role="group" aria-label="Filtrar por estado">
        <button
          type="button"
          className="contador"
          aria-pressed={estado === ""}
          onClick={() => setEstado("")}
        >
          <span className="contador__numero">{turnos.length}</span>
          <span className="contador__etiqueta">Todos</span>
        </button>
        {Object.entries(ESTADOS_TURNO).map(([clave, info]) => (
          <button
            key={clave}
            type="button"
            className={`contador contador--${info.variante}`}
            aria-pressed={estado === clave}
            onClick={() => setEstado(estado === clave ? "" : clave)}
          >
            <span className="contador__numero">{conteo[clave]}</span>
            <span className="contador__etiqueta">{info.etiqueta}</span>
          </button>
        ))}
      </div>

      <div className="tarjeta barra-filtros">
        <label htmlFor="filtro-fecha">Fecha</label>
        <input id="filtro-fecha" type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} />
        <button type="button" className="boton-secundario" onClick={() => setFecha(hoyIso())}>
          Hoy
        </button>
        <button type="button" className="boton-secundario" onClick={() => setFecha("")} disabled={!fecha}>
          Todas las fechas
        </button>
        <button type="button" className="boton-secundario barra-filtros__actualizar" onClick={cargar}>
          Actualizar
        </button>
      </div>

      {respuesta?.error && (
        <div className="tarjeta resultado resultado--error">
          <h3>No se pudieron cargar los turnos</h3>
          <p>{respuesta.error}</p>
        </div>
      )}

      {!respuesta && <div className="tarjeta">Cargando turnos…</div>}

      {respuesta?.turnos && (
        <div className="tabla-turnos">
          <table>
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Galpón</th>
                <th>Responsable</th>
                <th>Inicio</th>
                <th>Cierre</th>
                <th className="numero">% mortalidad</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {visibles.map((t) => (
                <tr key={t.id} className={t.estado === "ABIERTO" ? "fila-pendiente" : undefined}>
                  <td>{formatearFecha(t.fecha)}</td>
                  <td>
                    {t.galponNombre}
                    <small>{t.loteId}</small>
                  </td>
                  <td>{t.responsableNombre}</td>
                  <td>{formatearHora(t.horaInicio)}</td>
                  <td>{formatearHora(t.horaCierre)}</td>
                  <td className="numero">
                    {t.porcentajeMortalidad != null ? `${t.porcentajeMortalidad.toFixed(2)}%` : "—"}
                  </td>
                  <td>
                    <EstadoTurno estado={t.estado} />
                  </td>
                </tr>
              ))}
              {visibles.length === 0 && (
                <tr>
                  <td colSpan="7" className="tabla-turnos__vacia">
                    No hay turnos {estado ? `en estado "${ESTADOS_TURNO[estado].etiqueta}"` : ""}
                    {fecha ? ` el ${formatearFecha(fecha)}` : ""}.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
