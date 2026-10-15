import { useCallback, useEffect, useState } from "react";
import {
  asignarGalpon,
  finalizarLote,
  listarGalpones,
  listarWorkers,
  registrarGalpon,
  registrarLote,
} from "../api/galpones";
import { useSesion } from "../auth/SesionContext";

const ESTADOS_GALPON = {
  ACTIVO: { etiqueta: "Activo", variante: "exito" },
  EN_RIESGO_SANITARIO: { etiqueta: "En riesgo sanitario", variante: "peligro" },
};

const ESTADOS_LOTE = {
  ACTIVO: { etiqueta: "Activo", variante: "exito" },
  EN_OBSERVACION: { etiqueta: "En observación", variante: "pendiente" },
  FINALIZADO: { etiqueta: "Finalizado", variante: "aprobado" },
};

function Estado({ estados, valor }) {
  const info = estados[valor] ?? { etiqueta: valor, variante: "pendiente" };
  return <span className={`estado-turno estado-turno--${info.variante}`}>{info.etiqueta}</span>;
}

/** Traduce la respuesta del backend a un mensaje para mostrar, o null si salió bien. */
async function ejecutar(accion, estadoOk) {
  try {
    const { status, data } = await accion();
    if (status === estadoOk) return null;
    return data?.mensaje ?? `Respuesta inesperada del servidor (HTTP ${status})`;
  } catch {
    return "No se pudo conectar con el servidor.";
  }
}

/**
 * F-11.2: cada galpón con su lote activo, su estado y los workers asignados.
 * Con GESTIONAR_GALPONES (Administrador) se registran galpones y lotes, se
 * finalizan lotes y se asignan workers; con VER_LOTES la página es de solo lectura.
 */
export default function LotesPage() {
  const { tienePermiso } = useSesion();
  const puedeGestionar = tienePermiso("GESTIONAR_GALPONES");
  const [respuesta, setRespuesta] = useState(null); // { galpones, workers } | { error }

  const cargar = useCallback(async () => {
    try {
      const [galpones, workers] = await Promise.all([
        listarGalpones(),
        puedeGestionar ? listarWorkers() : Promise.resolve({ status: 200, data: [] }),
      ]);
      if (galpones.status !== 200) {
        setRespuesta({ error: galpones.data?.mensaje ?? `Respuesta inesperada del servidor (HTTP ${galpones.status})` });
      } else {
        setRespuesta({ galpones: galpones.data, workers: workers.status === 200 ? workers.data : [] });
      }
    } catch {
      setRespuesta({ error: "No se pudo conectar con el servidor. Verifica que el backend esté corriendo." });
    }
  }, [puedeGestionar]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const galpones = respuesta?.galpones ?? [];
  const workers = respuesta?.workers ?? [];
  const conLote = galpones.filter((g) => g.loteActivo).length;
  const enRiesgo = galpones.filter((g) => g.estado === "EN_RIESGO_SANITARIO").length;
  const aves = galpones.reduce((total, g) => total + (g.loteActivo?.poblacionActual ?? 0), 0);
  const nombreGalpon = Object.fromEntries(galpones.map((g) => [g.id, g.nombre]));

  return (
    <div className="pagina pagina--ancha">
      <header className="pagina__encabezado">
        <h1>Lotes y galpones</h1>
        <p>
          Cada galpón aloja un solo lote activo, sin superar su capacidad máxima.
          {!puedeGestionar && " Vista de solo lectura."}
        </p>
      </header>

      {respuesta?.galpones && (
        <div className="filtro-estados">
          <div className="contador">
            <span className="contador__numero">{galpones.length}</span>
            <span className="contador__etiqueta">Galpones</span>
          </div>
          <div className="contador contador--exito">
            <span className="contador__numero">{conLote}</span>
            <span className="contador__etiqueta">Con lote activo</span>
          </div>
          <div className="contador contador--pendiente">
            <span className="contador__numero">{galpones.length - conLote}</span>
            <span className="contador__etiqueta">Libres</span>
          </div>
          <div className="contador contador--peligro">
            <span className="contador__numero">{enRiesgo}</span>
            <span className="contador__etiqueta">En riesgo sanitario</span>
          </div>
          <div className="contador contador--aprobado">
            <span className="contador__numero">{aves.toLocaleString("es-CO")}</span>
            <span className="contador__etiqueta">Aves alojadas</span>
          </div>
        </div>
      )}

      {!respuesta && <div className="tarjeta">Cargando galpones…</div>}

      {respuesta?.error && (
        <div className="tarjeta resultado resultado--error">
          <h3>No se pudieron cargar los galpones</h3>
          <p>{respuesta.error}</p>
        </div>
      )}

      {puedeGestionar && respuesta?.galpones && <FormularioGalpon onRegistrado={cargar} />}

      {respuesta?.galpones?.length === 0 && (
        <div className="tarjeta">
          <p>Aún no hay galpones registrados.</p>
        </div>
      )}

      <div className="galpones">
        {galpones.map((galpon) => (
          <TarjetaGalpon
            key={galpon.id}
            galpon={galpon}
            workers={workers}
            nombreGalpon={nombreGalpon}
            puedeGestionar={puedeGestionar}
            onCambio={cargar}
          />
        ))}
      </div>
    </div>
  );
}

function FormularioGalpon({ onRegistrado }) {
  const [nombre, setNombre] = useState("");
  const [capacidad, setCapacidad] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  async function manejarEnvio(evento) {
    evento.preventDefault();
    setEnviando(true);
    const mensaje = await ejecutar(
      () => registrarGalpon({ nombre, capacidadMaxima: capacidad === "" ? null : Number(capacidad) }),
      201,
    );
    setEnviando(false);
    setError(mensaje);
    if (!mensaje) {
      setNombre("");
      setCapacidad("");
      onRegistrado();
    }
  }

  return (
    <form className="tarjeta galpon-form" onSubmit={manejarEnvio}>
      <h3>Registrar galpón</h3>
      <div className="galpon-form__campos">
        <div className="campo">
          <label htmlFor="galpon-nombre">Nombre</label>
          <input
            id="galpon-nombre"
            required
            placeholder="Ej: Galpón 5"
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
          />
        </div>
        <div className="campo">
          <label htmlFor="galpon-capacidad">Capacidad máxima (aves)</label>
          <input
            id="galpon-capacidad"
            type="number"
            min="1"
            required
            value={capacidad}
            onChange={(e) => setCapacidad(e.target.value)}
          />
        </div>
        <button type="submit" className="boton-secundario boton-secundario--principal" disabled={enviando}>
          {enviando ? "Registrando…" : "Registrar galpón"}
        </button>
      </div>
      {error && (
        <p className="login__error" role="alert">
          {error}
        </p>
      )}
    </form>
  );
}

function TarjetaGalpon({ galpon, workers, nombreGalpon, puedeGestionar, onCambio }) {
  const [error, setError] = useState(null);
  const lote = galpon.loteActivo;
  const ocupacion = lote ? Math.round((lote.poblacionActual / galpon.capacidadMaxima) * 100) : 0;

  async function accion(fn, estadoOk) {
    const mensaje = await ejecutar(fn, estadoOk);
    setError(mensaje);
    if (!mensaje) onCambio();
    return !mensaje;
  }

  return (
    <article className="tarjeta galpon">
      <div className="galpon__cabecera">
        <div>
          <h3 className="galpon__nombre">{galpon.nombre}</h3>
          <span className="galpon__detalle">
            Capacidad {galpon.capacidadMaxima.toLocaleString("es-CO")} aves · <code>{galpon.id}</code>
          </span>
        </div>
        <Estado estados={ESTADOS_GALPON} valor={galpon.estado} />
      </div>

      <section className="galpon__seccion">
        <h4>Lote activo</h4>
        {lote ? (
          <>
            <div className="galpon__lote">
              <code>{lote.id}</code>
              <Estado estados={ESTADOS_LOTE} valor={lote.estado} />
            </div>
            <dl className="galpon__datos">
              <div>
                <dt>Aves vivas</dt>
                <dd>
                  {lote.poblacionActual.toLocaleString("es-CO")}
                  <small> de {lote.poblacionInicial.toLocaleString("es-CO")}</small>
                </dd>
              </div>
              <div>
                <dt>Edad</dt>
                <dd>
                  {lote.edadSemanas} semana{lote.edadSemanas === 1 ? "" : "s"}
                </dd>
              </div>
              <div>
                <dt>Ocupación</dt>
                <dd>{ocupacion}%</dd>
              </div>
            </dl>
            <div className="galpon__barra" aria-hidden="true">
              <span style={{ width: `${Math.min(ocupacion, 100)}%` }} />
            </div>
            {puedeGestionar && (
              <BotonFinalizar onConfirmar={() => accion(() => finalizarLote(lote.id), 200)} />
            )}
          </>
        ) : (
          <>
            <p className="galpon__vacio">Sin lote: el galpón está libre.</p>
            {puedeGestionar && (
              <FormularioLote
                galpon={galpon}
                onRegistrar={(datos) => accion(() => registrarLote(galpon.id, datos), 201)}
              />
            )}
          </>
        )}
      </section>

      <section className="galpon__seccion">
        <h4>Workers asignados</h4>
        {galpon.workers.length === 0 && <p className="galpon__vacio">Ningún worker asignado.</p>}
        <ul className="galpon__workers">
          {galpon.workers.map((w) => (
            <li key={w.username}>
              <span>
                {w.nombreCompleto} <small>{w.username}</small>
              </span>
              {puedeGestionar && (
                <button
                  type="button"
                  className="boton-secundario galpon__quitar"
                  onClick={() => accion(() => asignarGalpon(w.username, null), 200)}
                  aria-label={`Quitar a ${w.nombreCompleto} del ${galpon.nombre}`}
                >
                  Quitar
                </button>
              )}
            </li>
          ))}
        </ul>
        {puedeGestionar && (
          <FormularioAsignacion
            galpon={galpon}
            workers={workers}
            nombreGalpon={nombreGalpon}
            onAsignar={(username) => accion(() => asignarGalpon(username, galpon.id), 200)}
          />
        )}
      </section>

      {error && (
        <p className="login__error" role="alert">
          {error}
        </p>
      )}
    </article>
  );
}

function BotonFinalizar({ onConfirmar }) {
  const [confirmando, setConfirmando] = useState(false);
  const [enviando, setEnviando] = useState(false);

  async function confirmar() {
    setEnviando(true);
    await onConfirmar();
    setEnviando(false);
    setConfirmando(false);
  }

  if (!confirmando) {
    return (
      <button type="button" className="boton-secundario" onClick={() => setConfirmando(true)}>
        Finalizar lote
      </button>
    );
  }
  return (
    <div className="galpon__confirmar">
      <span>¿Finalizar el lote? El galpón quedará libre.</span>
      <button type="button" className="boton-secundario boton-secundario--principal" onClick={confirmar} disabled={enviando}>
        {enviando ? "Finalizando…" : "Sí, finalizar"}
      </button>
      <button type="button" className="boton-secundario" onClick={() => setConfirmando(false)} disabled={enviando}>
        Cancelar
      </button>
    </div>
  );
}

function FormularioLote({ galpon, onRegistrar }) {
  const [poblacion, setPoblacion] = useState("");
  const [edad, setEdad] = useState("0");
  const [enviando, setEnviando] = useState(false);

  async function manejarEnvio(evento) {
    evento.preventDefault();
    setEnviando(true);
    const ok = await onRegistrar({
      poblacionInicial: poblacion === "" ? null : Number(poblacion),
      edadSemanas: edad === "" ? null : Number(edad),
    });
    setEnviando(false);
    if (ok) setPoblacion("");
  }

  return (
    <form className="galpon__formulario" onSubmit={manejarEnvio}>
      <div className="campo">
        <label htmlFor={`poblacion-${galpon.id}`}>Población inicial</label>
        <input
          id={`poblacion-${galpon.id}`}
          type="number"
          min="1"
          required
          value={poblacion}
          onChange={(e) => setPoblacion(e.target.value)}
        />
        <span className="ayuda-campo">Máximo {galpon.capacidadMaxima.toLocaleString("es-CO")} aves</span>
      </div>
      <div className="campo">
        <label htmlFor={`edad-${galpon.id}`}>Edad (semanas)</label>
        <input
          id={`edad-${galpon.id}`}
          type="number"
          min="0"
          required
          value={edad}
          onChange={(e) => setEdad(e.target.value)}
        />
      </div>
      <button type="submit" className="boton-secundario boton-secundario--principal" disabled={enviando}>
        {enviando ? "Registrando…" : "Registrar lote"}
      </button>
    </form>
  );
}

function FormularioAsignacion({ galpon, workers, nombreGalpon, onAsignar }) {
  const [username, setUsername] = useState("");
  const [enviando, setEnviando] = useState(false);
  const disponibles = workers.filter((w) => w.galponAsignadoId !== galpon.id);

  if (disponibles.length === 0) return null;

  async function manejarEnvio(evento) {
    evento.preventDefault();
    setEnviando(true);
    const ok = await onAsignar(username);
    setEnviando(false);
    if (ok) setUsername("");
  }

  return (
    <form className="galpon__asignar" onSubmit={manejarEnvio}>
      <label htmlFor={`asignar-${galpon.id}`} className="sr-only">
        Worker para asignar al {galpon.nombre}
      </label>
      <select id={`asignar-${galpon.id}`} required value={username} onChange={(e) => setUsername(e.target.value)}>
        <option value="">Asignar worker…</option>
        {disponibles.map((w) => (
          <option key={w.username} value={w.username}>
            {w.nombreCompleto}
            {w.galponAsignadoId ? ` (hoy en ${nombreGalpon[w.galponAsignadoId] ?? w.galponAsignadoId})` : " (sin galpón)"}
          </option>
        ))}
      </select>
      <button type="submit" className="boton-secundario" disabled={!username || enviando}>
        Asignar
      </button>
    </form>
  );
}
