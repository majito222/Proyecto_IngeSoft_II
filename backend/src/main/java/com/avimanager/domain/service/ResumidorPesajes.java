package com.avimanager.domain.service;

import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.ResumenPesaje;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

/**
 * Arma el ResumenPesaje con los nombres que necesita la pantalla. Lo comparten
 * los servicios de F-07.
 */
class ResumidorPesajes {

    private final GalponRepositoryPort galponRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    ResumidorPesajes(GalponRepositoryPort galponRepository, UsuarioRepositoryPort usuarioRepository) {
        this.galponRepository = galponRepository;
        this.usuarioRepository = usuarioRepository;
    }

    ResumenPesaje resumir(Pesaje pesaje) {
        String galponNombre = galponRepository.buscarPorId(pesaje.getGalponId())
                .map(Galpon::getNombre)
                .orElse(pesaje.getGalponId());
        String revisadoPor = pesaje.getRevisadoPor() == null ? null : nombreDe(pesaje.getRevisadoPor());
        return new ResumenPesaje(pesaje, galponNombre, nombreDe(pesaje.getRegistradoPor()), revisadoPor);
    }

    private String nombreDe(String username) {
        return usuarioRepository.buscarPorUsername(username).map(Usuario::getNombreCompleto).orElse(username);
    }
}
