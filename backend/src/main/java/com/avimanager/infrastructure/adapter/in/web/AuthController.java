package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.model.Rol;
import com.avimanager.domain.port.in.IniciarSesionCommand;
import com.avimanager.domain.port.in.IniciarSesionUseCase;
import com.avimanager.infrastructure.adapter.in.web.dto.LoginRequest;
import com.avimanager.infrastructure.adapter.in.web.dto.LoginResponse;
import com.avimanager.infrastructure.adapter.in.web.dto.UsuarioResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada HTTP para F-09 "Autenticar usuarios y gestionar roles".
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IniciarSesionUseCase iniciarSesionUseCase;

    public AuthController(IniciarSesionUseCase iniciarSesionUseCase) {
        this.iniciarSesionUseCase = iniciarSesionUseCase;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return LoginResponse.desde(iniciarSesionUseCase.iniciarSesion(
                new IniciarSesionCommand(request.getUsername(), request.getContrasena())));
    }

    /** Devuelve el usuario del token actual; el frontend lo usa para validar una sesion guardada. */
    @GetMapping("/me")
    public UsuarioResponse usuarioActual(@AuthenticationPrincipal Jwt jwt) {
        return new UsuarioResponse(jwt.getSubject(), jwt.getClaimAsString("nombre"),
                Rol.valueOf(jwt.getClaimAsString("rol")));
    }
}
