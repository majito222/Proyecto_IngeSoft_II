package com.avimanager.domain.service;

import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class FakeUsuarioRepository implements UsuarioRepositoryPort {

    private final Map<String, Usuario> almacen = new HashMap<>();

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return Optional.ofNullable(almacen.get(username));
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        almacen.put(usuario.getUsername(), usuario);
        return usuario;
    }
}
