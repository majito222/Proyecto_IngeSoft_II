import { useCallback, useEffect, useState } from "react";
import { consultarGalponesParaReporte, reportarDano } from "../api/mantenimiento";
import EstadoOrden, { TIPOS_DANO, formatearPlazo } from "../components/EstadoOrden";
import { formatearFecha, formatearHora } from "../components/EstadoTurno";

const ESTADO_GALPON = {
  ACTIVO: { etiqueta: "Activo", variante: "exito" },
  EN_RIESGO_SANITARIO: { etiqueta: "En riesgo sanitario", variante: "peligro" },
};

/**
 * F-08.2: el worker reporta un daño de infraestructura en su galpón asignado.
 * F-08.1: el galpón pasa a "En riesgo sanitario" y se crea una orden de
 * mantenimiento con plazo de 12 h (RN-04).
 */
export default function ReportarDanoPage() {
  const [respuesta, setRespuesta] = useState(null); // { galpones } | { error }
  const [reportada, setReportada] = useState(null);

  const cargar = useCallback(async () => {
    try {
      const { status, data } = await consultarGalponesParaReporte();
      if (status === 200) setRespuesta({ galpones: data });
      else setRespuesta({ error: data?.mensaje ?? `Respuesta inesperada del servidor (HTTP ${status})` });
    } catch {
      setRespuesta({ error: "No se pudo conectar con el servidor. Verifica que el backend esté corriendo." });
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  async function manejarReportada(orden) {
    setReportada(orden);
    await cargar();
  }

  const galpones = respuesta?.galpones ?? [];

  return (
    <div className="pagina">
      <header className="pagina__encabezado">
        <h1>Reportar daño de infraestructura</h1>
        <p>
          Reporta fallas en mallas, techos o cortinas. El galpón queda en riesgo sanitario y el técnico recibe una
          orden con plazo de 12 horas (RN-04).
        </p>
      </header>

      {!respuesta && <div className="tarjeta">Cargando galpones…</div>}

      {respuesta?.error && (
        <div className="tarjeta resultado resultado--error">
          <h3>No se pudieron cargar los galpones</h3>
          <p>{respuesta.error}</p>
        </div>
      )}

      {respuesta?.galpones?.length === 0 && (
        <div className="tarjeta resultado resultado--advertencia">
          <h3>No tienes un galpón asignado</h3>
          <p>Pide al administrador que te asigne un galpón para poder reportar daños.</p>
        </div>
      )}

      {galpones.length > 0 && <FormularioDano galpon={galpones[0]} onReportada={manejarReportada} />}

      {reportada && (
        <div className="tarjeta resultado resultado--advertencia" role="status">
          <h3>Daño reportado</h3>
          <p>
            {reportada.galponNombre} quedó <strong>en riesgo sanitario</strong>. Se creó la orden de{" "}
            {reportada.tipoDanoDescripcion.toLowerCase()} con prioridad {reportada.prioridad.toLowerCase()} y se avisó
            al técnico. Debe repararse antes de las {formatearHora(reportada.fechaLimite)} del{" "}
            {formatearFecha(reportada.fechaLimite)}.
          </p>
        </div>
      )}

      {galpones.map((g) => (
        <section key={g.galponId} className="tarjeta" aria-label={`Estado de ${g.galponNombre}`}>
          <div className="alerta__cabecera">
            <strong className="alerta__galpon">{g.galponNombre}</strong>
            <span className={`estado-turno estado-turno--${ESTADO_GALPON[g.estado]?.variante ?? "pendiente"}`}>
              {ESTADO_GALPON[g.estado]?.etiqueta ?? g.estado}
            </span>
          </div>
          {g.ordenesPendientes.length === 0 ? (
            <p className="alerta__aviso">Sin órdenes de mantenimiento pendientes.</p>
          ) : (
            <ul className="orden__lista">
              {g.ordenesPendientes.map((o) => (
                <li key={o.id}>
                  <span>
                    <strong>{o.tipoDanoDescripcion}</strong> · {o.descripcion}
                  </span>
                  <span className="orden__lista-estado">
                    <EstadoOrden estado={o.estado} />
                    <small className={o.vencida ? "orden__plazo--vencida" : ""}>
                      {formatearPlazo(o.minutosRestantes)}
                    </small>
                  </span>
                </li>
              ))}
            </ul>
          )}
        </section>
      ))}
    </div>
  );
}

function FormularioDano({ galpon, onReportada }) {
  const [tipoDano, setTipoDano] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  async function manejarEnvio(evento) {
    evento.preventDefault();
    setEnviando(true);
    setError(null);
    try {
      const { status, data } = await reportarDano({ tipoDano, descripcion });
      if (status === 201) {
        setTipoDano("");
        setDescripcion("");
        await onReportada(data);
      } else {
        setError(data?.mensaje ?? `No se pudo reportar el daño (HTTP ${status})`);
      }
    } catch {
      setError("No se pudo conectar con el servidor.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="tarjeta" onSubmit={manejarEnvio}>
      <div className="pesaje__campos">
        <div className="campo">
          <label htmlFor="galpon-dano">Galpón</label>
          <input id="galpon-dano" type="text" value={galpon.galponNombre} readOnly />
        </div>
        <div className="campo">
          <label htmlFor="tipo-dano">
            Tipo de daño <span className="requerido">*</span>
          </label>
          <select id="tipo-dano" required value={tipoDano} onChange={(e) => setTipoDano(e.target.value)}>
            <option value="" disabled>
              Selecciona…
            </option>
            {TIPOS_DANO.map((t) => (
              <option key={t.valor} value={t.valor}>
                {t.etiqueta}
              </option>
            ))}
          </select>
        </div>
      </div>
      <div className="campo">
        <label htmlFor="descripcion-dano">
          Descripción <span className="requerido">*</span>
        </label>
        <textarea
          id="descripcion-dano"
          rows="3"
          required
          placeholder="Ej: malla perimetral rota en el costado norte"
          value={descripcion}
          onChange={(e) => setDescripcion(e.target.value)}
        />
      </div>
      {error && (
        <p className="login__error" role="alert">
          {error}
        </p>
      )}
      <button type="submit" className="boton-primario" disabled={enviando}>
        {enviando ? "Reportando…" : "Reportar daño"}
      </button>
    </form>
  );
}
