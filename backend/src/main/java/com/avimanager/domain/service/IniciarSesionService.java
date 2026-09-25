package com.avimanager.domain.service;

import com.avimanager.domain.exception.CredencialesInvalidasException;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.IniciarSesionCommand;
import com.avimanager.domain.port.in.IniciarSesionUseCase;
import com.avimanager.domain.port.in.SesionIniciada;
import com.avimanager.domain.port.out.CifradorContrasenaPort;
import com.avimanager.domain.port.out.GeneradorTokenPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

/**
 * Implementa F-09: valida las credenciales y emite el token de sesion con el
 * rol del usuario. Igual que CerrarTurnoService, no conoce Spring ni JWT:
 * solo depende de los puertos del dominio.
 */
public class IniciarSesionService implements IniciarSesionUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final CifradorContrasenaPort cifrador;
    private final GeneradorTokenPort generadorToken;

    public IniciarSesionService(UsuarioRepositoryPort usuarioRepository,
                                CifradorContrasenaPort cifrador,
                                GeneradorTokenPort generadorToken) {
        this.usuarioRepository = usuarioRepository;
        this.cifrador = cifrador;
        this.generadorToken = generadorToken;
    }

    @Override
    public SesionIniciada iniciarSesion(IniciarSesionCommand comando) {
        if (comando.getUsername() == null || comando.getContrasena() == null) {
            throw new CredencialesInvalidasException();
        }

        Usuario usuario = usuarioRepository.buscarPorUsername(comando.getUsername().trim())
                .filter(Usuario::isActivo)
                .filter(u -> cifrador.coincide(comando.getContrasena(), u.getContrasenaCifrada()))
                .orElseThrow(CredencialesInvalidasException::new);

        return new SesionIniciada(generadorToken.generarToken(usuario), usuario);
    }
}
