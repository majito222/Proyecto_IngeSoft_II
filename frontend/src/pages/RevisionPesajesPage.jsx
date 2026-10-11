import { useCallback, useEffect, useState } from "react";
import { listarPesajes, revisarPesaje } from "../api/pesajes";
import EstadoPesaje, { formatearDesviacion, formatearGramos } from "../components/EstadoPesaje";
import { formatearFecha } from "../components/EstadoTurno";

/**
 * F-07.2: el zootecnista ve los pesajes de todos los galpones, con los que se
 * salen de ±5% resaltados en amarillo, y decide si se ajusta la ración (RN-02).
 */
export default function RevisionPesajesPage() {
  const [respuesta, setRespuesta] = useState(null); // { pesajes } | { error }
  const [enCurso, setEnCurso] = useState(null); // id del pesaje que se está revisando
  const [error, setError] = useState(null);

  const cargar = useCallback(async () => {
    try {
      const { status, data } = await listarPesajes();
      if (status === 200) setRespuesta({ pesajes: data });
      else setRespuesta({ error: data?.mensaje ?? `Respuesta inesperada del servidor (HTTP ${status})` });
    } catch {
      setRespuesta({ error: "No se pudo conectar con el servidor. Verifica que el backend esté corriendo." });
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  async function revisar(pesaje, aprobarAjuste) {
    setEnCurso(pesaje.id);
    setError(null);
    try {
      const { status, data } = await revisarPesaje(pesaje.id, aprobarAjuste);
      if (status === 200) {
        setRespuesta(({ pesajes }) => ({ pesajes: pesajes.map((p) => (p.id === data.id ? data : p)) }));
      } else {
        setError(data?.mensaje ?? `No se pudo registrar la revisión (HTTP ${status})`);
      }
    } catch {
      setError("No se pudo conectar con el servidor.");
    } finally {
      setEnCurso(null);
    }
  }

  const pesajes = respuesta?.pesajes ?? [];
  const pendientes = pesajes.filter((p) => p.estado === "PENDIENTE_REVISION").length;
  const ajustados = pesajes.filter((p) => p.estado === "AJUSTE_APROBADO").length;

  return (
    <div className="pagina pagina--ancha">
      <header className="pagina__encabezado">
        <h1>Revisión de pesajes</h1>
        <p>
          Pesajes semanales de los galpones. Los que se desvían más de ±5% del peso estándar quedan pendientes hasta
          que decidas si se ajusta la ración (RN-02).
        </p>
      </header>

      {!respuesta && <div className="tarjeta">Cargando pesajes…</div>}

      {respuesta?.error && (
        <div className="tarjeta resultado resultado--error">
          <h3>No se pudieron cargar los pesajes</h3>
          <p>{respuesta.error}</p>
        </div>
      )}

      {respuesta?.pesajes && (
        <>
          <div className="filtro-estados">
            <div className="contador contador--pendiente">
              <span className="contador__numero">{pendientes}</span>
              <span className="contador__etiqueta">Pendientes de revisión</span>
            </div>
            <div className="contador contador--aprobado">
              <span className="contador__numero">{ajustados}</span>
              <span className="contador__etiqueta">Con ajuste de ración</span>
            </div>
            <div className="contador">
              <span className="contador__numero">{pesajes.length}</span>
              <span className="contador__etiqueta">Pesajes registrados</span>
            </div>
          </div>

          {error && (
            <p className="tarjeta login__error" role="alert">
              {error}
            </p>
          )}

          <div className="tabla-turnos">
            <table>
              <thead>
                <tr>
                  <th>Galpón</th>
                  <th>Fecha</th>
                  <th className="numero">Peso promedio</th>
                  <th className="numero">Estándar</th>
                  <th className="numero">Desviación</th>
                  <th>Estado</th>
                  <th>Decisión</th>
                </tr>
              </thead>
              <tbody>
                {pesajes.length === 0 && (
                  <tr>
                    <td colSpan="7" className="tabla-turnos__vacia">
                      Todavía no hay pesajes registrados.
                    </td>
                  </tr>
                )}
                {pesajes.map((p) => (
                  <tr key={p.id} className={p.fueraDeRango ? "fila-desviada" : ""}>
                    <td>
                      {p.galponNombre}
                      <small>
                        {p.loteId} · semana {p.edadSemanas}
                      </small>
                    </td>
                    <td>
                      {formatearFecha(p.fecha)}
                      <small>{p.registradoPorNombre}</small>
                    </td>
                    <td className="numero">
                      {formatearGramos(p.pesoPromedioGramos)}
                      <small>{p.tamanoMuestra} aves</small>
                    </td>
                    <td className="numero">{formatearGramos(p.pesoEstandarGramos)}</td>
                    <td className={`numero ${p.fueraDeRango ? "pesaje__desviacion--fuera" : ""}`}>
                      {formatearDesviacion(p.desviacionPorcentaje)}
                    </td>
                    <td>
                      <EstadoPesaje estado={p.estado} />
                    </td>
                    <td>
                      {p.estado === "PENDIENTE_REVISION" ? (
                        <div className="pesaje__acciones">
                          <button
                            type="button"
                            className="boton-secundario boton-secundario--principal"
                            disabled={enCurso === p.id}
                            onClick={() => revisar(p, true)}
                          >
                            Aprobar ajuste de ración
                          </button>
                          <button
                            type="button"
                            className="boton-secundario"
                            disabled={enCurso === p.id}
                            onClick={() => revisar(p, false)}
                          >
                            Mantener ración
                          </button>
                        </div>
                      ) : p.revisadoPorNombre ? (
                        <small>
                          {p.revisadoPorNombre} · {formatearFecha(p.fechaRevision)}
                        </small>
                      ) : (
                        <small>No requiere revisión</small>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
