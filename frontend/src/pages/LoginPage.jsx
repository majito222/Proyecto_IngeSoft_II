import { useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { useSesion } from "../auth/SesionContext";

export default function LoginPage() {
  const { usuario, login } = useSesion();
  const navigate = useNavigate();
  const location = useLocation();
  const [username, setUsername] = useState("");
  const [contrasena, setContrasena] = useState("");
  const [error, setError] = useState(null);
  const [enviando, setEnviando] = useState(false);

  if (usuario) {
    return <Navigate to={location.state?.desde ?? "/"} replace />;
  }

  async function manejarEnvio(evento) {
    evento.preventDefault();
    setEnviando(true);
    setError(null);
    try {
      await login(username, contrasena);
      navigate(location.state?.desde ?? "/", { replace: true });
    } catch (e) {
      setError(e instanceof TypeError ? "No se pudo conectar con el servidor" : e.message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="login">
      <form className="tarjeta login__tarjeta" onSubmit={manejarEnvio}>
        <div className="login__marca">
          <span className="sidebar__logo">🐔</span>
          <div>
            <h1>AviManager</h1>
            <p>Inicia sesión para continuar</p>
          </div>
        </div>

        <div className="campo">
          <label htmlFor="username">Usuario</label>
          <input
            id="username"
            type="text"
            autoComplete="username"
            autoFocus
            required
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
        </div>

        <div className="campo">
          <label htmlFor="contrasena">Contraseña</label>
          <input
            id="contrasena"
            type="password"
            autoComplete="current-password"
            required
            value={contrasena}
            onChange={(e) => setContrasena(e.target.value)}
          />
        </div>

        {error && (
          <p className="login__error" role="alert">
            {error}
          </p>
        )}

        <button type="submit" className="boton-primario" disabled={enviando}>
          {enviando ? "Ingresando..." : "Ingresar"}
        </button>
      </form>
    </div>
  );
}
