package com.avimanager.domain.service;

import com.avimanager.domain.port.out.CifradorContrasenaPort;

/**
 * Doble de prueba: "cifra" anteponiendo un prefijo, suficiente para probar el
 * caso de uso sin depender de BCrypt.
 */
class FakeCifradorContrasena implements CifradorContrasenaPort {

    @Override
    public String cifrar(String contrasenaPlana) {
        return "cifrada:" + contrasenaPlana;
    }

    @Override
    public boolean coincide(String contrasenaPlana, String contrasenaCifrada) {
        return cifrar(contrasenaPlana).equals(contrasenaCifrada);
    }
}
