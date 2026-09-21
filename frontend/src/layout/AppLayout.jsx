import { useState } from "react";
import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar";
import { IconMenu } from "../components/icons";

export default function AppLayout() {
  const [menuAbierto, setMenuAbierto] = useState(false);

  return (
    <div className={"layout" + (menuAbierto ? " layout--menu-abierto" : "")}>
      <header className="topbar">
        <button
          type="button"
          className="topbar__toggle"
          aria-label="Abrir menú"
          onClick={() => setMenuAbierto((v) => !v)}
        >
          <IconMenu />
        </button>
        <strong>AviManager</strong>
      </header>

      <div className="layout__sidebar">
        <Sidebar onNavigate={() => setMenuAbierto(false)} />
      </div>

      {menuAbierto && (
        <button
          type="button"
          className="layout__overlay"
          aria-label="Cerrar menú"
          onClick={() => setMenuAbierto(false)}
        />
      )}

      <main className="layout__contenido">
        <Outlet />
      </main>
    </div>
  );
}
