package com.avimanager.domain.service;

import com.avimanager.domain.exception.CredencialesInvalidasException;
import com.avimanager.domain.model.Rol;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.IniciarSesionCommand;
import com.avimanager.domain.port.in.SesionIniciada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas del caso de uso F-09 con dobles de prueba de los puertos, sin Spring.
 */
class IniciarSesionServiceTest {

    private FakeUsuarioRepository usuarioRepository;
    private FakeCifradorContrasena cifrador;
    private IniciarSesionService service;

    @BeforeEach
    void setUp() {
        usuarioRepository = new FakeUsuarioRepository();
        cifrador = new FakeCifradorContrasena();
        service = new IniciarSesionService(usuarioRepository, cifrador, new FakeGeneradorToken());

        usuarioRepository.guardar(new Usuario("u1", "operario", "Pedro Operario",
                cifrador.cifrar("secreta"), Rol.OPERARIO, true));
        usuarioRepository.guardar(new Usuario("u2", "inactivo", "Usuario Inactivo",
                cifrador.cifrar("secreta"), Rol.ADMINISTRADOR, false));
    }

    @Test
    void emiteTokenConElRolCuandoLasCredencialesSonValidas() {
        SesionIniciada sesion = service.iniciarSesion(new IniciarSesionCommand("operario", "secreta"));

        assertThat(sesion.getToken()).isEqualTo("token-operario-OPERARIO");
        assertThat(sesion.getUsuario().getRol()).isEqualTo(Rol.OPERARIO);
    }

    @Test
    void rechazaContrasenaIncorrecta() {
        assertThatThrownBy(() -> service.iniciarSesion(new IniciarSesionCommand("operario", "otra")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void rechazaUsuarioInexistente() {
        assertThatThrownBy(() -> service.iniciarSesion(new IniciarSesionCommand("nadie", "secreta")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void rechazaUsuarioInactivoAunqueLaContrasenaSeaCorrecta() {
        assertThatThrownBy(() -> service.iniciarSesion(new IniciarSesionCommand("inactivo", "secreta")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void rechazaCredencialesVacias() {
        assertThatThrownBy(() -> service.iniciarSesion(new IniciarSesionCommand(null, null)))
                .isInstanceOf(CredencialesInvalidasException.class);
    }
}
