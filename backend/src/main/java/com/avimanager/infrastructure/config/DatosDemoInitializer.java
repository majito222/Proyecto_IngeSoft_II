package com.avimanager.infrastructure.config;

import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Rol;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.out.CifradorContrasenaPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Carga un galpon/lote de ejemplo al arrancar, unicamente para poder probar
 * F-01/F-02 por API sin depender todavia del modulo de gestion de galpones (F-06).
 * IDs fijos para facilitar las pruebas manuales: galpon-1 / lote-1.
 *
 * Tambien crea un usuario de demo por cada rol (F-09). La contraseña de cada
 * uno es su nombre de usuario seguido de "123" (ej. operario / operario123).
 */
@Component
public class DatosDemoInitializer implements CommandLineRunner {

    private final LoteRepositoryPort loteRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final CifradorContrasenaPort cifrador;

    public DatosDemoInitializer(LoteRepositoryPort loteRepository,
                                UsuarioRepositoryPort usuarioRepository,
                                CifradorContrasenaPort cifrador) {
        this.loteRepository = loteRepository;
        this.usuarioRepository = usuarioRepository;
        this.cifrador = cifrador;
    }

    @Override
    public void run(String... args) {
        loteRepository.guardar(new Lote("lote-1", "galpon-1", 500, 500, 3, EstadoLote.ACTIVO));

        crearUsuario("operario", "Pedro Operario", Rol.OPERARIO);
        crearUsuario("admin", "Ana Administradora", Rol.ADMINISTRADOR);
        crearUsuario("veterinario", "Valeria Veterinaria", Rol.VETERINARIO);
        crearUsuario("dueno", "Diego Dueño", Rol.DUENO);
        crearUsuario("zootecnista", "Zoe Zootecnista", Rol.ZOOTECNISTA);
        crearUsuario("tecnico", "Tomás Técnico", Rol.TECNICO);
    }

    private void crearUsuario(String username, String nombre, Rol rol) {
        usuarioRepository.guardar(new Usuario("usr-" + username, username, nombre,
                cifrador.cifrar(username + "123"), rol, true));
    }
}
