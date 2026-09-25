package com.avimanager.infrastructure.adapter.out.security;

import com.avimanager.domain.port.out.CifradorContrasenaPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: cifra contraseñas con BCrypt (hash con sal, no reversible).
 */
@Component
public class BCryptCifradorContrasenaAdapter implements CifradorContrasenaPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String cifrar(String contrasenaPlana) {
        return encoder.encode(contrasenaPlana);
    }

    @Override
    public boolean coincide(String contrasenaPlana, String contrasenaCifrada) {
        return encoder.matches(contrasenaPlana, contrasenaCifrada);
    }
}
