package com.avimanager.domain.port.out;

/**
 * Cifrado de contraseñas. El dominio no sabe si es BCrypt, Argon2, etc.
 */
public interface CifradorContrasenaPort {

    String cifrar(String contrasenaPlana);

    boolean coincide(String contrasenaPlana, String contrasenaCifrada);
}
