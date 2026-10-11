import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { consultarReferenciaPesaje, registrarPesaje } from "../api/pesajes";
import { useSesion } from "../auth/SesionContext";
import EstadoPesaje, { TOLERANCIA_PESO, formatearDesviacion, formatearGramos } from "../components/EstadoPesaje";
import { formatearFecha } from "../components/EstadoTurno";

/**
 * F-07.1: el worker pesa una muestra de aves de su galpón asignado una vez por
 * semana. F-07.2: si el peso se desvía más de ±5% del estándar (RN-02), el
 * registro se resalta en amarillo y queda pendiente para el zootecnista.
 */
export default function PesajePage() {
  const { tienePermiso } = useSesion();
  const [referencia, setReferencia] = useState(null); // { datos } | { status, aviso } | { errorRed }

  const cargar = useCallback(async () => {
    try {
      const { status, data } = await consultarReferenciaPesaje();
      if (status === 200) setReferencia({ datos: data });
      else setReferencia({ status, aviso: data?.mensaje ?? `Respuesta inesperada del servidor (HTTP ${status})` });
    } catch {
      setReferencia({ errorRed: true });
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const datos = referencia?.datos;
  // El administrador no tiene galpón asignado: revisa los pesajes de todos.
  const esSupervisorSinGalpon = referencia?.status === 409 && tienePermiso("REVISAR_PESAJES");

  function registrado(pesaje) {
    setReferencia(({ datos: d }) => ({ datos: { ...d, pesajeDeLaSemana: pesaje } }));
  }

  return (
    <div className="pagina">
      <header className="pagina__encabezado">
        <h1>Pesaje semanal</h1>
        <p>Pesa una muestra de aves de tu galpón y compárala con el peso estándar de la semana (RN-02).</p>
      </header>

      {!referencia && <div className="tarjeta">Cargando tu galpón asignado…</div>}

      {referencia?.errorRed && (
        <div className="tarjeta resultado resultado--error">
          <h3>No se pudo conectar con el servidor</h3>
          <p>Verifica que el backend esté corriendo en el puerto 8080.</p>
        </div>
      )}

      {esSupervisorSinGalpon && (
        <div className="tarjeta resultado resultado--exito aviso-supervisor">
          <h3>Revisas los pesajes de todos los galpones</h3>
          <p>
            Como administrador no tienes un galpón asignado, así que no registras pesajes. Los workers pesan su
            galpón y tú puedes revisar los que se salen de rango.
          </p>
          <Link to="/pesajes" className="boton-primario enlace-boton">
            Ir a Revisión de pesajes
          </Link>
        </div>
      )}

      {referencia?.aviso && !esSupervisorSinGalpon && (
        <div className="tarjeta resultado resultado--advertencia">
          <h3>No puedes registrar el pesaje</h3>
          <p>{referencia.aviso}</p>
        </div>
      )}

      {datos && (
        <section className="tarjeta turno-galpon" aria-label="Galpón asignado">
          <div className="turno-galpon__datos">
            <span className="turno-galpon__etiqueta">Tu galpón</span>
            <strong className="turno-galpon__nombre">{datos.galponNombre}</strong>
            <span className="turno-galpon__detalle">
              Lote <code>{datos.loteId}</code> · {datos.avesVivas.toLocaleString("es-CO")} aves · {datos.edadSemanas}{" "}
              semanas
            </span>
          </div>
          <div className="turno-galpon__estado">
            <span className="turno-galpon__etiqueta">Peso estándar</span>
            <strong className="pesaje__estandar">{formatearGramos(datos.pesoEstandarGramos)}</strong>
            <small>tolerancia ±{TOLERANCIA_PESO}%</small>
          </div>
        </section>
      )}

      {datos && !datos.pesajeDeLaSemana && (
        <FormularioPesaje pesoEstandar={datos.pesoEstandarGramos} onRegistrado={registrado} />
      )}

      {datos?.pesajeDeLaSemana && <ResultadoPesaje pesaje={datos.pesajeDeLaSemana} />}
    </div>
  );
}

function FormularioPesaje({ pesoEstandar, onRegistrado }) {
  const [peso, setPeso] = useState("");
  const [muestra, setMuestra] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  const pesoNumero = Number(peso);
  const desviacion = peso !== "" && pesoNumero > 0 ? ((pesoNumero - pesoEstandar) / pesoEstandar) * 100 : null;
  const fueraDeRango = desviacion !== null && Math.abs(Math.round(desviacion * 100) / 100) > TOLERANCIA_PESO;

  async function manejarEnvio(evento) {
    evento.preventDefault();
    setEnviando(true);
    setError(null);
    try {
      const { status, data } = await registrarPesaje({
        pesoPromedioGramos: pesoNumero,
        tamanoMuestra: Number(muestra),
      });
      if (status === 201) onRegistrado(data);
      else setError(data?.mensaje ?? `No se pudo registrar el pesaje (HTTP ${status})`);
    } catch {
      setError("No se pudo conectar con el servidor.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className={`tarjeta pesaje__formulario ${fueraDeRango ? "pesaje--desviado" : ""}`} onSubmit={manejarEnvio}>
      <div className="pesaje__campos">
        <div className="campo">
          <label htmlFor="peso-promedio">
            Peso promedio de la muestra (g) <span className="requerido">*</span>
          </label>
          <input
            id="peso-promedio"
            type="number"
            min="1"
            step="0.1"
            required
            inputMode="decimal"
            placeholder={`Ej: ${pesoEstandar}`}
            value={peso}
            onChange={(e) => setPeso(e.target.value)}
          />
        </div>
        <div className="campo">
          <label htmlFor="tamano-muestra">
            Aves pesadas <span className="requerido">*</span>
          </label>
          <input
            id="tamano-muestra"
            type="number"
            min="1"
            step="1"
            required
            inputMode="numeric"
            placeholder="Ej: 30"
            value={muestra}
            onChange={(e) => setMuestra(e.target.value)}
          />
        </div>
      </div>

      {desviacion !== null && (
        <p className={`pesaje__vista-previa ${fueraDeRango ? "pesaje__vista-previa--fuera" : ""}`} role="status">
          Desviación frente al estándar: <strong>{formatearDesviacion(desviacion)}</strong>
          {fueraDeRango
            ? " · fuera de rango: el zootecnista revisará el ajuste de ración."
            : " · dentro de rango."}
        </p>
      )}

      {error && (
        <p className="login__error" role="alert">
          {error}
        </p>
      )}
      <button type="submit" className="boton-primario" disabled={enviando}>
        {enviando ? "Registrando…" : "Registrar pesaje"}
      </button>
    </form>
  );
}

function ResultadoPesaje({ pesaje }) {
  const fuera = pesaje.fueraDeRango;
  return (
    <article className={`tarjeta resultado ${fuera ? "resultado--advertencia pesaje--desviado" : "resultado--exito"}`}>
      <div className="alerta__cabecera">
        <div>
          <h3 className="pesaje__titulo">Pesaje de esta semana</h3>
          <span className="alerta__detalle">
            {formatearFecha(pesaje.fecha)} · {pesaje.tamanoMuestra} aves · registrado por {pesaje.registradoPorNombre}
          </span>
        </div>
        <EstadoPesaje estado={pesaje.estado} />
      </div>
      <dl className="alerta__atencion">
        <div>
          <dt>Peso promedio</dt>
          <dd>{formatearGramos(pesaje.pesoPromedioGramos)}</dd>
        </div>
        <div>
          <dt>Estándar semana {pesaje.edadSemanas}</dt>
          <dd>{formatearGramos(pesaje.pesoEstandarGramos)}</dd>
        </div>
        <div>
          <dt>Desviación</dt>
          <dd className={fuera ? "pesaje__desviacion--fuera" : ""}>{formatearDesviacion(pesaje.desviacionPorcentaje)}</dd>
        </div>
      </dl>
      <p className="alerta__aviso">
        {pesaje.estado === "PENDIENTE_REVISION" &&
          "El peso se salió del rango de ±5%. Se avisó al zootecnista para que decida si se ajusta la ración."}
        {pesaje.estado === "AJUSTE_APROBADO" &&
          `${pesaje.revisadoPorNombre} aprobó ajustar la ración: la ración diaria del lote ya se recalculó.`}
        {pesaje.estado === "AJUSTE_DESCARTADO" && `${pesaje.revisadoPorNombre} decidió mantener la ración estándar.`}
        {pesaje.estado === "DENTRO_DE_RANGO" && "El lote está dentro del rango. El próximo pesaje es la otra semana."}
      </p>
    </article>
  );
}
