import { useCallback, useEffect, useState } from "react";
import { atenderAlerta, listarAlertas } from "../api/alertas";
import { useSesion } from "../auth/SesionContext";
import EstadoTurno, { formatearFecha, formatearHora } from "../components/EstadoTurno";

/**
 * F-02.6: alertas sanitarias generadas por RN-01. Las pendientes van primero.
 * F-02.5: quien tiene ATENDER_ALERTAS_SANITARIAS registra diagnóstico y
 * tratamiento, y el turno bloqueado queda cerrado con alerta.
 */
export default function AlertasPage() {
  const { tienePermiso } = useSesion();
  const puedeAtender = tienePermiso("ATENDER_ALERTAS_SANITARIAS");
  const [respuesta, setRespuesta] = useState(null); // { alertas } | { error }

  const cargar = useCallback(async () => {
    try {
      const { status, data } = await listarAlertas();
      if (status === 200) setRespuesta({ alertas: data });
      else setRespuesta({ error: data?.mensaje ?? `Respuesta inesperada del servidor (HTTP ${status})` });
    } catch {
      setRespuesta({ error: "No se pudo conectar con el servidor. Verifica que el backend esté corriendo." });
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  function reemplazar(alertaActualizada) {
    setRespuesta(({ alertas }) => ({
      alertas: alertas.map((a) => (a.id === alertaActualizada.id ? alertaActualizada : a)),
    }));
  }

  const alertas = [...(respuesta?.alertas ?? [])].sort(
    (a, b) => (a.estado === "PENDIENTE" ? 0 : 1) - (b.estado === "PENDIENTE" ? 0 : 1),
  );
  const pendientes = alertas.filter((a) => a.estado === "PENDIENTE").length;

  return (
    <div className="pagina">
      <header className="pagina__encabezado">
        <h1>Alertas sanitarias</h1>
        <p>
          Alertas generadas cuando la mortalidad diaria supera el 1.5% (RN-01).
          {respuesta?.alertas && ` ${pendientes} pendiente${pendientes === 1 ? "" : "s"} de atención.`}
        </p>
      </header>

      {!respuesta && <div className="tarjeta">Cargando alertas…</div>}

      {respuesta?.error && (
        <div className="tarjeta resultado resultado--error">
          <h3>No se pudieron cargar las alertas</h3>
          <p>{respuesta.error}</p>
        </div>
      )}

      {respuesta?.alertas?.length === 0 && (
        <div className="tarjeta">
          <p>No hay alertas sanitarias. Aparecen aquí cuando un cierre de turno supera el umbral de mortalidad.</p>
        </div>
      )}

      {alertas.map((alerta) => (
        <TarjetaAlerta key={alerta.id} alerta={alerta} puedeAtender={puedeAtender} onAtendida={reemplazar} />
      ))}
    </div>
  );
}

function TarjetaAlerta({ alerta, puedeAtender, onAtendida }) {
  const pendiente = alerta.estado === "PENDIENTE";

  return (
    <article className={`tarjeta alerta ${pendiente ? "alerta--pendiente" : "alerta--atendida"}`}>
      <div className="alerta__cabecera">
        <div>
          <strong className="alerta__galpon">{alerta.galponNombre ?? alerta.loteId}</strong>
          <span className="alerta__detalle">
            Lote <code>{alerta.loteId}</code> · {formatearFecha(alerta.fechaHora)} {formatearHora(alerta.fechaHora)}
            {alerta.responsableTurnoNombre && ` · Turno de ${alerta.responsableTurnoNombre}`}
          </span>
        </div>
        <div className="alerta__estado">
          <span className="alerta__porcentaje">{alerta.porcentajeMortalidad.toFixed(2)}%</span>
          <span className={`estado-turno estado-turno--${pendiente ? "peligro" : "exito"}`}>
            {pendiente ? "Pendiente" : "Atendida"}
          </span>
        </div>
      </div>

      {!pendiente && (
        <dl className="alerta__atencion">
          <div>
            <dt>Diagnóstico</dt>
            <dd>{alerta.diagnostico}</dd>
          </div>
          <div>
            <dt>Tratamiento</dt>
            <dd>{alerta.tratamiento}</dd>
          </div>
          <div>
            <dt>Atendida por</dt>
            <dd>
              {alerta.atendidaPorNombre} · {formatearFecha(alerta.fechaAtencion)} {formatearHora(alerta.fechaAtencion)}
            </dd>
          </div>
          {alerta.estadoTurno && (
            <div>
              <dt>Turno</dt>
              <dd>
                <EstadoTurno estado={alerta.estadoTurno} />
              </dd>
            </div>
          )}
        </dl>
      )}

      {pendiente && puedeAtender && <FormularioAtencion alerta={alerta} onAtendida={onAtendida} />}
      {pendiente && !puedeAtender && (
        <p className="alerta__aviso">El turno sigue bloqueado hasta que el veterinario atienda esta alerta.</p>
      )}
    </article>
  );
}

function FormularioAtencion({ alerta, onAtendida }) {
  const [diagnostico, setDiagnostico] = useState("");
  const [tratamiento, setTratamiento] = useState("");
  const [liberarLote, setLiberarLote] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  async function manejarEnvio(evento) {
    evento.preventDefault();
    setEnviando(true);
    setError(null);
    try {
      const { status, data } = await atenderAlerta(alerta.id, { diagnostico, tratamiento, liberarLote });
      if (status === 200) onAtendida(data);
      else setError(data?.mensaje ?? `No se pudo registrar la atención (HTTP ${status})`);
    } catch {
      setError("No se pudo conectar con el servidor.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="alerta__formulario" onSubmit={manejarEnvio}>
      <div className="campo">
        <label htmlFor={`diagnostico-${alerta.id}`}>Diagnóstico</label>
        <textarea
          id={`diagnostico-${alerta.id}`}
          rows="2"
          required
          placeholder="Ej: bronquitis infecciosa"
          value={diagnostico}
          onChange={(e) => setDiagnostico(e.target.value)}
        />
      </div>
      <div className="campo">
        <label htmlFor={`tratamiento-${alerta.id}`}>Tratamiento</label>
        <textarea
          id={`tratamiento-${alerta.id}`}
          rows="2"
          required
          placeholder="Ej: vacuna de refuerzo y aislamiento del lote"
          value={tratamiento}
          onChange={(e) => setTratamiento(e.target.value)}
        />
      </div>
      <label className="alerta__casilla" htmlFor={`liberar-${alerta.id}`}>
        <input
          id={`liberar-${alerta.id}`}
          type="checkbox"
          checked={liberarLote}
          onChange={(e) => setLiberarLote(e.target.checked)}
        />
        Liberar el lote de observación
      </label>
      {error && (
        <p className="login__error" role="alert">
          {error}
        </p>
      )}
      <button type="submit" className="boton-primario" disabled={enviando}>
        {enviando ? "Registrando…" : "Registrar atención y cerrar el turno"}
      </button>
    </form>
  );
}
