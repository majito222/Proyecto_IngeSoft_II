import { useCallback, useEffect, useState } from "react";
import { listarInventario } from "../api/inventario";

const TIPOS_INSUMO = {
  ALIMENTO: "Alimento",
  SANIDAD: "Sanidad",
  MATERIAL_CAMA: "Material de cama",
};

const UMBRAL_CRITICO = 50;

function formatear(numero) {
  return numero.toLocaleString("es-CO", { maximumFractionDigits: 1 });
}

/**
 * F-03.2: la bodega con el stock, la capacidad y el % disponible de cada
 * insumo. Bajo el 50% el insumo queda en "Stock Crítico" (RN-03). El cierre de
 * turno descuenta automáticamente el alimento consumido.
 */
export default function InventarioPage() {
  const [respuesta, setRespuesta] = useState(null); // { insumos } | { error }

  const cargar = useCallback(async () => {
    try {
      const { status, data } = await listarInventario();
      if (status === 200) setRespuesta({ insumos: data });
      else setRespuesta({ error: data?.mensaje ?? `Respuesta inesperada del servidor (HTTP ${status})` });
    } catch {
      setRespuesta({ error: "No se pudo conectar con el servidor. Verifica que el backend esté corriendo." });
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const insumos = respuesta?.insumos ?? [];
  const criticos = insumos.filter((i) => i.critico).length;

  return (
    <div className="pagina pagina--ancha">
      <header className="pagina__encabezado">
        <h1>Inventario</h1>
        <p>
          Stock de la bodega. El cierre de cada turno descuenta el alimento consumido, y un insumo por debajo del{" "}
          {UMBRAL_CRITICO}% de su capacidad queda en Stock Crítico.
        </p>
      </header>

      {respuesta?.insumos && (
        <div className="filtro-estados">
          <div className="contador">
            <span className="contador__numero">{insumos.length}</span>
            <span className="contador__etiqueta">Insumos en bodega</span>
          </div>
          <div className={`contador ${criticos > 0 ? "contador--peligro" : "contador--exito"}`}>
            <span className="contador__numero">{criticos}</span>
            <span className="contador__etiqueta">En Stock Crítico</span>
          </div>
          <div className="barra-filtros__actualizar inventario__actualizar">
            <button type="button" className="boton-secundario" onClick={cargar}>
              Actualizar
            </button>
          </div>
        </div>
      )}

      {!respuesta && <div className="tarjeta">Cargando inventario…</div>}

      {respuesta?.error && (
        <div className="tarjeta resultado resultado--error">
          <h3>No se pudo cargar el inventario</h3>
          <p>{respuesta.error}</p>
        </div>
      )}

      {respuesta?.insumos?.length === 0 && (
        <div className="tarjeta">
          <p>La bodega no tiene insumos registrados.</p>
        </div>
      )}

      <div className="inventario">
        {insumos.map((insumo) => (
          <TarjetaInsumo key={insumo.id} insumo={insumo} />
        ))}
      </div>
    </div>
  );
}

function TarjetaInsumo({ insumo }) {
  const porcentaje = Math.min(insumo.porcentajeDisponible, 100);

  return (
    <article className={`tarjeta insumo ${insumo.critico ? "insumo--critico" : ""}`}>
      <div className="insumo__cabecera">
        <div>
          <h3 className="insumo__nombre">{insumo.nombre}</h3>
          <span className="insumo__tipo">{TIPOS_INSUMO[insumo.tipo] ?? insumo.tipo}</span>
        </div>
        <span className={`estado-turno estado-turno--${insumo.critico ? "peligro" : "exito"}`}>
          {insumo.critico ? "Stock Crítico" : "Disponible"}
        </span>
      </div>

      <div className="insumo__porcentaje">
        {formatear(insumo.porcentajeDisponible)}%<small> disponible</small>
      </div>

      <div
        className="insumo__barra"
        role="meter"
        aria-label={`${insumo.nombre}: ${formatear(insumo.porcentajeDisponible)}% disponible`}
        aria-valuemin="0"
        aria-valuemax="100"
        aria-valuenow={Math.round(porcentaje)}
      >
        <span className="insumo__relleno" style={{ width: `${porcentaje}%` }} />
        <span className="insumo__umbral" style={{ left: `${UMBRAL_CRITICO}%` }} title="Umbral de Stock Crítico (50%)" />
      </div>

      <dl className="insumo__datos">
        <div>
          <dt>Stock</dt>
          <dd>
            {formatear(insumo.stock)} {insumo.unidad}
          </dd>
        </div>
        <div>
          <dt>Capacidad</dt>
          <dd>
            {formatear(insumo.capacidad)} {insumo.unidad}
          </dd>
        </div>
      </dl>

      {insumo.critico && (
        <p className="insumo__aviso">Por debajo del {UMBRAL_CRITICO}% de la capacidad: hay que reabastecer.</p>
      )}
    </article>
  );
}
