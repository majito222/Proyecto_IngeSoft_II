package com.avimanager.domain.port.out;

import com.avimanager.domain.model.Usuario;

import java.util.Optional;

public interface UsuarioRepositoryPort {

    Optional<Usuario> buscarPorUsername(String username);

    Usuario guardar(Usuario usuario);
}
