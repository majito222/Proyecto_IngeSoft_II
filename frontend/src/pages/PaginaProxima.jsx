export default function PaginaProxima({ icono: Icono, titulo, descripcion, funcionalidades }) {
  return (
    <div className="pagina">
      <header className="pagina__encabezado">
        <h1>{titulo}</h1>
        <p>{descripcion}</p>
      </header>

      <div className="tarjeta proxima">
        <div className="proxima__icono">
          <Icono width="32" height="32" />
        </div>
        <span className="badge">Próximamente</span>
        <p>
          Esta sección todavía no tiene endpoint en el backend. Se habilitará junto con
          las siguientes funcionalidades del <code>Plan_de_Requisitos_Galpon</code>:
        </p>
        <ul>
          {funcionalidades.map((f) => (
            <li key={f}>{f}</li>
          ))}
        </ul>
      </div>
    </div>
  );
}
