import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import SesionProvider from "./auth/SesionProvider";
import RutaProtegida from "./auth/RutaProtegida";
import AppLayout from "./layout/AppLayout";
import LoginPage from "./pages/LoginPage";
import InicioPage from "./pages/InicioPage";
import CierreTurnoPage from "./pages/CierreTurnoPage";
import LotesPage from "./pages/LotesPage";
import InventarioPage from "./pages/InventarioPage";
import IndicadoresPage from "./pages/IndicadoresPage";
import AlertasPage from "./pages/AlertasPage";
import TurnosPage from "./pages/TurnosPage";
import RacionPage from "./pages/RacionPage";
import PesajePage from "./pages/PesajePage";
import RevisionPesajesPage from "./pages/RevisionPesajesPage";
import ReportarDanoPage from "./pages/ReportarDanoPage";
import OrdenesMantenimientoPage from "./pages/OrdenesMantenimientoPage";
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
              path="turnos"
              element={
                <RutaProtegida permiso="VER_TURNOS">
                  <TurnosPage />
                </RutaProtegida>
              }
            />
            <Route
              path="cierre-turno"
              element={
                <RutaProtegida permiso="CERRAR_TURNO">
                  <CierreTurnoPage />
                </RutaProtegida>
              }
            />
            <Route
              path="pesaje"
              element={
                <RutaProtegida permiso="REGISTRAR_PESO">
                  <PesajePage />
                </RutaProtegida>
              }
            />
            <Route
              path="pesajes"
              element={
                <RutaProtegida permiso="REVISAR_PESAJES">
                  <RevisionPesajesPage />
                </RutaProtegida>
              }
            />
            <Route
              path="reportar-dano"
              element={
                <RutaProtegida permiso="REPORTAR_DANOS">
                  <ReportarDanoPage />
                </RutaProtegida>
              }
            />
            <Route
              path="ordenes"
              element={
                <RutaProtegida permiso="ATENDER_ORDENES_MANTENIMIENTO">
                  <OrdenesMantenimientoPage />
                </RutaProtegida>
              }
            />
            <Route
              path="racion"
              element={
                <RutaProtegida permiso="VER_RACION">
                  <RacionPage />
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
            <Route
              path="inventario"
              element={
                <RutaProtegida permiso="VER_INVENTARIO">
                  <InventarioPage />
                </RutaProtegida>
              }
            />
            <Route
              path="indicadores"
              element={
                <RutaProtegida permiso="VER_DASHBOARD_KPI">
                  <IndicadoresPage />
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
