package com.avimanager.infrastructure.adapter.out.persistence;

import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador de salida: usuarios en memoria. El nombre de usuario no distingue
 * mayusculas/minusculas.
 */
@Repository
public class InMemoryUsuarioRepository implements UsuarioRepositoryPort {

    private final Map<String, Usuario> almacen = new ConcurrentHashMap<>();

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return Optional.ofNullable(almacen.get(clave(username)));
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        almacen.put(clave(usuario.getUsername()), usuario);
        return usuario;
    }

    private static String clave(String username) {
        return username.toLowerCase(Locale.ROOT);
    }
}
