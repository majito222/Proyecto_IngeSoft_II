import { useState } from "react";
import { cerrarTurno } from "../api/turnos";
import CierreTurnoForm from "../components/CierreTurnoForm";
import ResultadoTurno from "../components/ResultadoTurno";

export default function CierreTurnoPage() {
  const [loteId, setLoteId] = useState("lote-1");
  const [enviando, setEnviando] = useState(false);
  const [resultado, setResultado] = useState(null);

  async function manejarEnvio(payload) {
    setEnviando(true);
    setResultado(null);
    try {
      const { status, data } = await cerrarTurno(loteId, payload);
      setResultado({ status, data });
    } catch {
      setResultado({ errorRed: true });
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="pagina">
      <header className="pagina__encabezado">
        <h1>Cierre de turno operativo</h1>
        <p>Registra el reporte diario del lote y valida la mortalidad (RN-01, RN-05, RN-07).</p>
      </header>

      <div className="tarjeta selector-lote">
        <label htmlFor="loteId">Lote</label>
        <input id="loteId" type="text" value={loteId} onChange={(e) => setLoteId(e.target.value)} />
      </div>

      <CierreTurnoForm onSubmit={manejarEnvio} enviando={enviando} />

      <ResultadoTurno resultado={resultado} />
    </div>
  );
}
