import { useState } from "react";

const CAMPOS_INICIALES = {
  consumoAlimentoKg: "",
  cantidadBajas: "",
  causaProbableMortalidad: "",
  produccionHuevosBandejas: "",
  novedades: "",
};

function aNumeroONull(valor) {
  if (valor === "" || valor === null || valor === undefined) return null;
  const numero = Number(valor);
  return Number.isNaN(numero) ? null : numero;
}

export default function CierreTurnoForm({ onSubmit, enviando }) {
  const [campos, setCampos] = useState(CAMPOS_INICIALES);

  const bajas = aNumeroONull(campos.cantidadBajas);
  const requiereCausa = bajas !== null && bajas > 0;

  function actualizarCampo(nombre, valor) {
    setCampos((prev) => ({ ...prev, [nombre]: valor }));
  }

  function manejarEnvio(evento) {
    evento.preventDefault();
    onSubmit({
      consumoAlimentoKg: aNumeroONull(campos.consumoAlimentoKg),
      cantidadBajas: aNumeroONull(campos.cantidadBajas),
      causaProbableMortalidad: campos.causaProbableMortalidad.trim() || null,
      produccionHuevosBandejas: aNumeroONull(campos.produccionHuevosBandejas),
      novedades: campos.novedades.trim() || null,
    });
  }

  return (
    <form className="tarjeta formulario-cierre" onSubmit={manejarEnvio}>
      <p className="ayuda">
        Diligencia el reporte diario del lote. Todos los campos son
        obligatorios, excepto novedades.
      </p>

      <div className="campo">
        <label htmlFor="consumoAlimentoKg">Consumo de alimento (kg)</label>
        <input
          id="consumoAlimentoKg"
          type="number"
          step="0.1"
          min="0"
          placeholder="Ej: 45.0"
          value={campos.consumoAlimentoKg}
          onChange={(e) => actualizarCampo("consumoAlimentoKg", e.target.value)}
        />
      </div>

      <div className="campo">
        <label htmlFor="cantidadBajas">Cantidad de bajas (aves muertas hoy)</label>
        <input
          id="cantidadBajas"
          type="number"
          step="1"
          min="0"
          placeholder="Ej: 5"
          value={campos.cantidadBajas}
          onChange={(e) => actualizarCampo("cantidadBajas", e.target.value)}
        />
      </div>

      <div className="campo">
        <label htmlFor="causaProbableMortalidad">
          Causa probable de mortalidad
          {requiereCausa && <span className="requerido"> *</span>}
        </label>
        <input
          id="causaProbableMortalidad"
          type="text"
          placeholder="Ej: jadeo, enfermedad..."
          value={campos.causaProbableMortalidad}
          onChange={(e) => actualizarCampo("causaProbableMortalidad", e.target.value)}
        />
        {requiereCausa && (
          <small className="ayuda-campo">
            Obligatoria porque se registraron bajas hoy.
          </small>
        )}
      </div>

      <div className="campo">
        <label htmlFor="produccionHuevosBandejas">Producción de huevos (bandejas)</label>
        <input
          id="produccionHuevosBandejas"
          type="number"
          step="1"
          min="0"
          placeholder="Ej: 12"
          value={campos.produccionHuevosBandejas}
          onChange={(e) => actualizarCampo("produccionHuevosBandejas", e.target.value)}
        />
      </div>

      <div className="campo">
        <label htmlFor="novedades">Novedades (opcional)</label>
        <textarea
          id="novedades"
          rows="3"
          placeholder="Observaciones del turno..."
          value={campos.novedades}
          onChange={(e) => actualizarCampo("novedades", e.target.value)}
        />
      </div>

      <button type="submit" className="boton-primario" disabled={enviando}>
        {enviando ? "Guardando..." : "Guardar y salir"}
      </button>
    </form>
  );
}
