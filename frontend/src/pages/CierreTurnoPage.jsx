import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { cerrarTurno, consultarTurnoActual, iniciarTurno } from "../api/turnos";
import { useSesion } from "../auth/SesionContext";
import CierreTurnoForm from "../components/CierreTurnoForm";
import EstadoTurno, { formatearHora } from "../components/EstadoTurno";
import ResultadoTurno from "../components/ResultadoTurno";

/**
 * F-10.2: el worker ve su galpón asignado e inicia el turno de hoy.
 * F-01 / F-02: con el turno ABIERTO diligencia el cierre.
 */
export default function CierreTurnoPage() {
  const { usuario, tienePermiso } = useSesion();
  const [actual, setActual] = useState(null); // { cargando } | { datos } | { aviso } | { errorRed }
  const [iniciando, setIniciando] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [resultado, setResultado] = useState(null);

  const cargar = useCallback(async () => {
    try {
      const { status, data } = await consultarTurnoActual();
      if (status === 200) setActual({ datos: data });
      else setActual({ status, aviso: data?.mensaje ?? `Respuesta inesperada del servidor (HTTP ${status})` });
    } catch {
      setActual({ errorRed: true });
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  async function manejarInicio() {
    setIniciando(true);
    setResultado(null);
    try {
      const { status, data } = await iniciarTurno();
      if (status === 201) setActual({ datos: data });
      else setResultado({ status, data });
    } catch {
      setResultado({ errorRed: true });
    } finally {
      setIniciando(false);
    }
  }

  async function manejarCierre(payload) {
    setEnviando(true);
    setResultado(null);
    try {
      const { status, data } = await cerrarTurno(actual.datos.loteId, payload);
      setResultado({ status, data });
      await cargar();
    } catch {
      setResultado({ errorRed: true });
    } finally {
      setEnviando(false);
    }
  }

  const datos = actual?.datos;
  const turno = datos?.turnoDeHoy;
  // Solo el responsable puede cerrar su turno (trazabilidad, F-10).
  const esMio = turno?.responsableUsername === usuario.username;
  // El administrador no tiene galpón asignado: supervisa desde el Panel de turnos (F-10.3).
  const esSupervisorSinGalpon = actual?.status === 409 && tienePermiso("VER_TURNOS");

  return (
    <div className="pagina">
      <header className="pagina__encabezado">
        <h1>Cierre de turno operativo</h1>
        <p>Inicia tu turno y registra el reporte diario del lote (RN-01, RN-05, RN-07).</p>
      </header>

      {!actual && <div className="tarjeta">Cargando tu galpón asignado…</div>}

      {actual?.errorRed && (
        <div className="tarjeta resultado resultado--error">
          <h3>No se pudo conectar con el servidor</h3>
          <p>Verifica que el backend esté corriendo en el puerto 8080.</p>
        </div>
      )}

      {esSupervisorSinGalpon && (
        <div className="tarjeta resultado resultado--exito aviso-supervisor">
          <h3>Supervisas los turnos desde el panel</h3>
          <p>
            Como administrador no tienes un galpón asignado, así que no inicias ni cierras turnos propios.
            Para ver el estado de los turnos de los workers usa el Panel de turnos.
          </p>
          <Link to="/turnos" className="boton-primario enlace-boton">
            Ir al Panel de turnos
          </Link>
        </div>
      )}

      {actual?.aviso && !esSupervisorSinGalpon && (
        <div className="tarjeta resultado resultado--advertencia">
          <h3>No puedes iniciar turno</h3>
          <p>{actual.aviso}</p>
        </div>
      )}

      {datos && (
        <section className="tarjeta turno-galpon" aria-label="Galpón asignado">
          <div className="turno-galpon__datos">
            <span className="turno-galpon__etiqueta">Tu galpón</span>
            <strong className="turno-galpon__nombre">{datos.galponNombre}</strong>
            <span className="turno-galpon__detalle">
              Lote <code>{datos.loteId}</code> · {datos.poblacionActual} aves · {datos.edadSemanas} semanas
              {datos.estadoLote === "EN_OBSERVACION" && " · lote en observación"}
            </span>
            {datos.racionSugeridaKg != null && (
              <span className="turno-galpon__racion">
                Ración sugerida hoy: <strong>{datos.racionSugeridaKg.toLocaleString("es-CO")} kg</strong>
              </span>
            )}
          </div>
          <div className="turno-galpon__estado">
            {turno ? (
              <>
                <EstadoTurno estado={turno.estado} />
                <small>
                  Iniciado {formatearHora(turno.horaInicio)} por {turno.responsableNombre}
                  {turno.horaCierre && ` · cerrado ${formatearHora(turno.horaCierre)}`}
                </small>
              </>
            ) : (
              <span className="estado-turno estado-turno--sin-iniciar">Sin iniciar</span>
            )}
          </div>
        </section>
      )}

      {datos && !turno && (
        <div className="tarjeta inicio-turno">
          <p>Todavía no has iniciado el turno de hoy. Al iniciarlo podrás registrar el cierre.</p>
          <button type="button" className="boton-primario" onClick={manejarInicio} disabled={iniciando}>
            {iniciando ? "Iniciando…" : "Iniciar turno"}
          </button>
        </div>
      )}

      {turno?.estado === "ABIERTO" && esMio && <CierreTurnoForm onSubmit={manejarCierre} enviando={enviando} />}

      {turno && !esMio && (
        <div className="tarjeta">
          <p>
            El turno de hoy de este galpón lo inició {turno.responsableNombre}. Solo esa persona puede
            registrar y cerrar el turno.
          </p>
        </div>
      )}

      {turno && esMio && turno.estado !== "ABIERTO" && !resultado && (
        <div className="tarjeta">
          <p>
            El turno de hoy ya no admite cambios.
            {turno.estado === "BLOQUEADO_ALERTA_SANITARIA" && " Espera las instrucciones del veterinario."}
          </p>
        </div>
      )}

      <ResultadoTurno resultado={resultado} />
    </div>
  );
}
