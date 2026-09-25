import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import SesionProvider from "./auth/SesionProvider";
import RutaProtegida from "./auth/RutaProtegida";
import AppLayout from "./layout/AppLayout";
import LoginPage from "./pages/LoginPage";
import InicioPage from "./pages/InicioPage";
import CierreTurnoPage from "./pages/CierreTurnoPage";
import LotesPage from "./pages/LotesPage";
import AlertasPage from "./pages/AlertasPage";
import "./App.css";

export default function App() {
  return (
    <SesionProvider>
      <BrowserRouter>
        <Routes>
          <Route path="login" element={<LoginPage />} />
          <Route
            element={
              <RutaProtegida>
                <AppLayout />
              </RutaProtegida>
            }
          >
            <Route index element={<InicioPage />} />
            <Route
              path="cierre-turno"
              element={
                <RutaProtegida permiso="CERRAR_TURNO">
                  <CierreTurnoPage />
                </RutaProtegida>
              }
            />
            <Route
              path="lotes"
              element={
                <RutaProtegida permiso="VER_LOTES">
                  <LotesPage />
                </RutaProtegida>
              }
            />
            <Route
              path="alertas"
              element={
                <RutaProtegida permiso="VER_ALERTAS_SANITARIAS">
                  <AlertasPage />
                </RutaProtegida>
              }
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </SesionProvider>
  );
}
