package com.avimanager.infrastructure.config;

import com.avimanager.domain.model.Permiso;
import com.avimanager.infrastructure.adapter.out.security.JwtGeneradorTokenAdapter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * F-09: la API es stateless. /api/auth/login es publico; el resto exige un JWT
 * valido en el header "Authorization: Bearer ...", y cada endpoint se autoriza
 * por permiso (ver Rol).
 */
@Configuration
public class SecurityConfig {

    private final SecretKey clave;

    public SecurityConfig(@Value("${avimanager.seguridad.jwt-secreto}") String secreto) {
        if (secreto.length() < 32) {
            throw new IllegalStateException("avimanager.seguridad.jwt-secreto debe tener al menos 32 caracteres");
        }
        this.clave = new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/lotes/*/turnos/cierre")
                            .hasAuthority(Permiso.CERRAR_TURNO.name())
                        .requestMatchers(HttpMethod.POST, "/api/turnos/inicio")
                            .hasAuthority(Permiso.INICIAR_TURNO.name())
                        .requestMatchers(HttpMethod.GET, "/api/turnos/actual")
                            .hasAuthority(Permiso.INICIAR_TURNO.name())
                        .requestMatchers(HttpMethod.GET, "/api/turnos")
                            .hasAuthority(Permiso.VER_TURNOS.name())
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorPermisos()))
                        .authenticationEntryPoint((req, res, ex) ->
                                responderJson(res, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Debes iniciar sesión para continuar")))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                responderJson(res, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Debes iniciar sesión para continuar"))
                        .accessDeniedHandler((req, res, e) ->
                                responderJson(res, HttpServletResponse.SC_FORBIDDEN,
                                        "Tu rol no tiene permiso para realizar esta acción")));
        return http.build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private JwtAuthenticationConverter convertidorPermisos() {
        JwtGrantedAuthoritiesConverter permisos = new JwtGrantedAuthoritiesConverter();
        permisos.setAuthoritiesClaimName(JwtGeneradorTokenAdapter.CLAIM_PERMISOS);
        permisos.setAuthorityPrefix("");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(permisos);
        return convertidor;
    }

    private static void responderJson(HttpServletResponse res, int status, String mensaje) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.getWriter().write("{\"mensaje\":\"" + mensaje + "\"}");
    }
}
