package com.avimanager.domain.service;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.port.out.NotificadorAlertaSanitariaPort;

import java.util.ArrayList;
import java.util.List;

class FakeNotificadorAlertaSanitaria implements NotificadorAlertaSanitariaPort {

    final List<AlertaSanitaria> notificadas = new ArrayList<>();

    @Override
    public void notificarAlVeterinario(AlertaSanitaria alerta, Lote lote) {
        notificadas.add(alerta);
    }
}
