import { BrowserRouter, Route, Routes } from "react-router-dom";
import AppLayout from "./layout/AppLayout";
import CierreTurnoPage from "./pages/CierreTurnoPage";
import LotesPage from "./pages/LotesPage";
import AlertasPage from "./pages/AlertasPage";
import "./App.css";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route index element={<CierreTurnoPage />} />
          <Route path="lotes" element={<LotesPage />} />
          <Route path="alertas" element={<AlertasPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
