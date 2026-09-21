const ETIQUETAS_CAMPOS = {
  consumoAlimentoKg: "Consumo de alimento (kg)",
  cantidadBajas: "Cantidad de bajas",
  causaProbableMortalidad: "Causa probable de mortalidad",
  produccionHuevosBandejas: "Producción de huevos (bandejas)",
};

function variantePorEstado(status, estado) {
  if (status === 409 || estado === "ALERTA_SANITARIA") return "peligro";
  if (status === 422 || estado === "CAMPOS_INCOMPLETOS") return "advertencia";
  if (status === 200 || estado === "TURNO_CERRADO") return "exito";
  return "error";
}

export default function ResultadoTurno({ resultado }) {
  if (!resultado) return null;

  const { status, data, errorRed } = resultado;

  if (errorRed) {
    return (
      <div className="tarjeta resultado resultado--error">
        <h3>No se pudo conectar con el servidor</h3>
        <p>Verifica que el backend esté corriendo en el puerto 8080.</p>
      </div>
    );
  }

  if (!data) {
    return (
      <div className="tarjeta resultado resultado--error">
        <h3>Respuesta inesperada del servidor (HTTP {status})</h3>
      </div>
    );
  }

  const variante = variantePorEstado(status, data.estado);

  return (
    <div className={`tarjeta resultado resultado--${variante}`}>
      <h3>{data.mensaje}</h3>

      {data.camposFaltantes?.length > 0 && (
        <ul className="lista-campos">
          {data.camposFaltantes.map((campo) => (
            <li key={campo}>{ETIQUETAS_CAMPOS[campo] ?? campo}</li>
          ))}
        </ul>
      )}

      {data.porcentajeMortalidad != null && (
        <p>
          <strong>% Mortalidad:</strong> {data.porcentajeMortalidad.toFixed(2)}%
        </p>
      )}
      {data.reporteId && (
        <p>
          <strong>Reporte:</strong> <code>{data.reporteId}</code>
        </p>
      )}
      {data.alertaId && (
        <p>
          <strong>Alerta sanitaria:</strong> <code>{data.alertaId}</code>
        </p>
      )}
    </div>
  );
}
