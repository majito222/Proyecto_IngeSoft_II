package com.avimanager.domain.port.out;

import com.avimanager.domain.model.AlertaSanitaria;

import java.util.List;
import java.util.Optional;

public interface AlertaSanitariaRepositoryPort {

    AlertaSanitaria guardar(AlertaSanitaria alertaSanitaria);

    Optional<AlertaSanitaria> buscarPorId(String alertaId);

    List<AlertaSanitaria> listarTodas();
}
