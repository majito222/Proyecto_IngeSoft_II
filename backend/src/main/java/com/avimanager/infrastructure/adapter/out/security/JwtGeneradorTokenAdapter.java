package com.avimanager.infrastructure.adapter.out.security;

import com.avimanager.domain.model.Permiso;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.out.GeneradorTokenPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Adaptador de salida: emite un JWT firmado (HS256) con el rol y los permisos
 * del usuario. SecurityConfig lee el claim "permisos" para autorizar cada endpoint.
 */
@Component
public class JwtGeneradorTokenAdapter implements GeneradorTokenPort {

    public static final String CLAIM_PERMISOS = "permisos";

    private final JwtEncoder encoder;
    private final Clock clock;
    private final Duration duracion;

    public JwtGeneradorTokenAdapter(JwtEncoder encoder, Clock clock,
                                    @Value("${avimanager.seguridad.jwt-duracion}") Duration duracion) {
        this.encoder = encoder;
        this.clock = clock;
        this.duracion = duracion;
    }

    @Override
    public String generarToken(Usuario usuario) {
        Instant ahora = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("avimanager")
                .subject(usuario.getUsername())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(duracion))
                .claim("nombre", usuario.getNombreCompleto())
                .claim("rol", usuario.getRol().name())
                .claim(CLAIM_PERMISOS, usuario.getRol().getPermisos().stream().map(Permiso::name).toList())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
