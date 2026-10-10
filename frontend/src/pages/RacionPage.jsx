import { useEffect, useState } from "react";
import { listarRaciones } from "../api/racion";

function formatearKg(kilos) {
  return `${kilos.toLocaleString("es-CO", { minimumFractionDigits: 1, maximumFractionDigits: 1 })} kg`;
}

/**
 * F-06.2: ración diaria sugerida por galpón = aves vivas × gramos por ave según
 * la estrategia de ración del lote (tabla de nutrición estándar por edad).
 */
export default function RacionPage() {
  const [respuesta, setRespuesta] = useState(null); // { raciones } | { error }

  useEffect(() => {
    listarRaciones()
      .then(({ status, data }) =>
        setRespuesta(
          status === 200
            ? { raciones: data }
            : { error: data?.mensaje ?? `Respuesta inesperada del servidor (HTTP ${status})` },
        ),
      )
      .catch(() => setRespuesta({ error: "No se pudo conectar con el servidor. Verifica que el backend esté corriendo." }));
  }, []);

  const raciones = respuesta?.raciones ?? [];
  const totalKg = raciones.reduce((suma, r) => suma + r.kilosTotales, 0);

  return (
    <div className="pagina pagina--ancha">
      <header className="pagina__encabezado">
        <h1>Ración diaria</h1>
        <p>Alimento sugerido para hoy en cada galpón, según la edad del lote y sus aves vivas.</p>
      </header>

      {!respuesta && <div className="tarjeta">Calculando raciones…</div>}

      {respuesta?.error && (
        <div className="tarjeta resultado resultado--error">
          <h3>No se pudo calcular la ración</h3>
          <p>{respuesta.error}</p>
        </div>
      )}

      {respuesta?.raciones && (
        <>
          <div className="filtro-estados">
            <div className="contador contador--exito">
              <span className="contador__numero">{formatearKg(Math.round(totalKg * 10) / 10)}</span>
              <span className="contador__etiqueta">Total de alimento hoy</span>
            </div>
            <div className="contador">
              <span className="contador__numero">{raciones.length}</span>
              <span className="contador__etiqueta">Galpones con lote</span>
            </div>
          </div>

          <div className="tabla-turnos">
            <table>
              <thead>
                <tr>
                  <th>Galpón</th>
                  <th className="numero">Edad</th>
                  <th className="numero">Aves vivas</th>
                  <th className="numero">g / ave / día</th>
                  <th className="numero">Ración de hoy</th>
                  <th>Cálculo</th>
                </tr>
              </thead>
              <tbody>
                {raciones.map((r) => (
                  <tr key={r.loteId}>
                    <td>
                      {r.galponNombre}
                      <small>{r.loteId}</small>
                    </td>
                    <td className="numero">{r.edadSemanas} sem</td>
                    <td className="numero">{r.avesVivas.toLocaleString("es-CO")}</td>
                    <td className="numero">{r.gramosPorAve} g</td>
                    <td className="numero">
                      <strong>{formatearKg(r.kilosTotales)}</strong>
                    </td>
                    <td>
                      <small className="racion__estrategia">{r.estrategia}</small>
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
