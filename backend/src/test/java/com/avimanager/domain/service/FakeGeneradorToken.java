package com.avimanager.domain.service;

import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.out.GeneradorTokenPort;

class FakeGeneradorToken implements GeneradorTokenPort {

    @Override
    public String generarToken(Usuario usuario) {
        return "token-" + usuario.getUsername() + "-" + usuario.getRol();
    }
}
