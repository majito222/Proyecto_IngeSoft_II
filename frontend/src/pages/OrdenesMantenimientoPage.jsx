import { useCallback, useEffect, useState } from "react";
import { certificarReparacion, iniciarReparacion, listarOrdenes } from "../api/mantenimiento";
import EstadoOrden, { formatearPlazo } from "../components/EstadoOrden";
import { formatearFecha, formatearHora } from "../components/EstadoTurno";

/**
 * F-08.3: bandeja del técnico. Las órdenes pendientes muestran las horas que
 * quedan del plazo de 12 h (RN-04) y las vencidas se marcan en rojo. Al
 * certificar la reparación, el galpón vuelve a "Activo" si no tiene otras
 * órdenes pendientes.
 */
export default function OrdenesMantenimientoPage() {
  const [respuesta, setRespuesta] = useState(null); // { ordenes } | { error }

  const cargar = useCallback(async () => {
    try {
      const { status, data } = await listarOrdenes();
      if (status === 200) setRespuesta({ ordenes: data });
      else setRespuesta({ error: data?.mensaje ?? `Respuesta inesperada del servidor (HTTP ${status})` });
    } catch {
      setRespuesta({ error: "No se pudo conectar con el servidor. Verifica que el backend esté corriendo." });
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const ordenes = respuesta?.ordenes ?? [];
  const pendientes = ordenes.filter((o) => o.estado !== "CERTIFICADA").length;
  const vencidas = ordenes.filter((o) => o.vencida).length;

  return (
    <div className="pagina">
      <header className="pagina__encabezado">
        <h1>Órdenes de mantenimiento</h1>
        <p>Daños de infraestructura reportados en los galpones. Cada orden debe repararse en 12 horas (RN-04).</p>
      </header>

      {!respuesta && <div className="tarjeta">Cargando órdenes…</div>}

      {respuesta?.error && (
        <div className="tarjeta resultado resultado--error">
          <h3>No se pudieron cargar las órdenes</h3>
          <p>{respuesta.error}</p>
        </div>
      )}

      {respuesta?.ordenes && (
        <div className="filtro-estados">
          <div className="contador contador--pendiente">
            <span className="contador__numero">{pendientes}</span>
            <span className="contador__etiqueta">Pendientes</span>
          </div>
          <div className="contador contador--peligro">
            <span className="contador__numero">{vencidas}</span>
            <span className="contador__etiqueta">Vencidas</span>
          </div>
          <div className="contador contador--exito">
            <span className="contador__numero">{ordenes.length - pendientes}</span>
            <span className="contador__etiqueta">Certificadas</span>
          </div>
        </div>
      )}

      {respuesta?.ordenes?.length === 0 && (
        <div className="tarjeta">
          <p>No hay órdenes de mantenimiento. Aparecen aquí cuando un worker reporta un daño.</p>
        </div>
      )}

      {ordenes.map((orden) => (
        <TarjetaOrden key={orden.id} orden={orden} onActualizada={cargar} />
      ))}
    </div>
  );
}

function TarjetaOrden({ orden, onActualizada }) {
  const [nota, setNota] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);
  const certificada = orden.estado === "CERTIFICADA";
  const variante = certificada ? "certificada" : orden.vencida ? "vencida" : "pendiente";

  async function ejecutar(accion) {
    setEnviando(true);
    setError(null);
    try {
      const { status, data } = await accion();
      if (status === 200) await onActualizada();
      else setError(data?.mensaje ?? `No se pudo actualizar la orden (HTTP ${status})`);
    } catch {
      setError("No se pudo conectar con el servidor.");
    } finally {
      setEnviando(false);
    }
  }

  function manejarCertificacion(evento) {
    evento.preventDefault();
    ejecutar(() => certificarReparacion(orden.id, nota));
  }

  return (
    <article className={`tarjeta alerta orden--${variante}`}>
      <div className="alerta__cabecera">
        <div>
          <strong className="alerta__galpon">
            {orden.galponNombre} · {orden.tipoDanoDescripcion}
          </strong>
          <span className="alerta__detalle">
            Reportada por {orden.reportadoPorNombre} el {formatearFecha(orden.fechaReporte)} a las{" "}
            {formatearHora(orden.fechaReporte)} · prioridad {orden.prioridad.toLowerCase()}
          </span>
        </div>
        <div className="alerta__estado">
          {!certificada && (
            <span className={`orden__plazo ${orden.vencida ? "orden__plazo--vencida" : ""}`}>
              {formatearPlazo(orden.minutosRestantes)}
            </span>
          )}
          <EstadoOrden estado={orden.estado} />
        </div>
      </div>

      <p className="orden__descripcion">{orden.descripcion}</p>

      {certificada && (
        <dl className="alerta__atencion">
          <div>
            <dt>Reparación</dt>
            <dd>{orden.notaReparacion}</dd>
          </div>
          <div>
            <dt>Certificada por</dt>
            <dd>
              {orden.tecnicoNombre} · {formatearFecha(orden.fechaCertificacion)}{" "}
              {formatearHora(orden.fechaCertificacion)}
            </dd>
          </div>
        </dl>
      )}

      {!certificada && (
        <form className="alerta__formulario" onSubmit={manejarCertificacion}>
          {orden.estado === "ABIERTA" ? (
            <button
              type="button"
              className="boton-secundario orden__iniciar"
              disabled={enviando}
              onClick={() => ejecutar(() => iniciarReparacion(orden.id))}
            >
              Iniciar reparación
            </button>
          ) : (
            <p className="alerta__aviso orden__iniciar">
              En reparación por {orden.tecnicoNombre} desde las {formatearHora(orden.fechaInicioReparacion)}.
            </p>
          )}
          <div className="campo">
            <label htmlFor={`nota-${orden.id}`}>Nota de reparación</label>
            <textarea
              id={`nota-${orden.id}`}
              rows="2"
              required
              placeholder="Ej: se reemplazó la malla del costado norte"
              value={nota}
              onChange={(e) => setNota(e.target.value)}
            />
          </div>
          {error && (
            <p className="login__error" role="alert">
              {error}
            </p>
          )}
          <button type="submit" className="boton-primario" disabled={enviando}>
            {enviando ? "Guardando…" : "Certificar reparación"}
          </button>
        </form>
      )}
    </article>
  );
}
