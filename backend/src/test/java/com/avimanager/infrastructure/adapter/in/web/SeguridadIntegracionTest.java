package com.avimanager.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de extremo a extremo de F-09: login real (BCrypt + JWT) y
 * autorizacion por rol sobre el endpoint de cierre de turno.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadIntegracionTest {

    private static final String CIERRE = "/api/lotes/lote-1/turnos/cierre";
    private static final String TURNO_NORMAL = """
            {"consumoAlimentoKg":45.0,"cantidadBajas":1,"causaProbableMortalidad":"jadeo","produccionHuevosBandejas":12}
            """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void loginConCredencialesValidasDevuelveTokenYPermisos() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"operario\",\"contrasena\":\"operario123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.rol").value("OPERARIO"))
                .andExpect(jsonPath("$.usuario.permisos[?(@ == 'CERRAR_TURNO')]").exists());
    }

    @Test
    void loginConContrasenaIncorrectaResponde401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"operario\",\"contrasena\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void cerrarTurnoSinTokenResponde401() throws Exception {
        mvc.perform(post(CIERRE).contentType(MediaType.APPLICATION_JSON).content(TURNO_NORMAL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cerrarTurnoConTokenInvalidoResponde401() throws Exception {
        mvc.perform(post(CIERRE).header("Authorization", "Bearer no-es-un-jwt")
                        .contentType(MediaType.APPLICATION_JSON).content(TURNO_NORMAL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void veterinarioNoPuedeCerrarTurno_responde403() throws Exception {
        mvc.perform(post(CIERRE).header("Authorization", "Bearer " + token("veterinario"))
                        .contentType(MediaType.APPLICATION_JSON).content(TURNO_NORMAL))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Tu rol no tiene permiso para realizar esta acción"));
    }

    @Test
    void operarioIniciaYCierraSuTurno_F10() throws Exception {
        String token = token("operario");
        mvc.perform(post("/api/turnos/inicio").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.galponNombre").value("Galpón 1"))
                .andExpect(jsonPath("$.turnoDeHoy.estado").value("ABIERTO"));

        mvc.perform(post(CIERRE).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(TURNO_NORMAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("TURNO_CERRADO"))
                .andExpect(jsonPath("$.estadoTurno").value("CERRADO"));
    }

    /**
     * Matriz rol → acceso al cierre de turno (permiso CERRAR_TURNO). Solo se
     * verifica la autorizacion, sobre un lote inexistente: con permiso la
     * peticion llega al caso de uso y responde 404; sin permiso, 403.
     */
    @ParameterizedTest(name = "{0} al cerrar turno → HTTP {1}")
    @CsvSource({
            "operario,    404",
            "admin,       403",
            "veterinario, 403",
            "dueno,       403",
            "zootecnista, 403",
            "tecnico,     403",
    })
    void soloElOperarioCierraTurno(String username, int statusEsperado) throws Exception {
        mvc.perform(post("/api/lotes/lote-inexistente/turnos/cierre")
                        .header("Authorization", "Bearer " + token(username))
                        .contentType(MediaType.APPLICATION_JSON).content(TURNO_NORMAL))
                .andExpect(status().is(statusEsperado));
    }

    /** El caso encontrado al probar: Olga no puede cerrar el turno abierto de Óscar (lote-3). */
    @Test
    void unOperarioNoPuedeCerrarElTurnoDeOtro_F10() throws Exception {
        mvc.perform(post("/api/lotes/lote-3/turnos/cierre").header("Authorization", "Bearer " + token("operario2"))
                        .contentType(MediaType.APPLICATION_JSON).content(TURNO_NORMAL))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Este turno es de operario3. Solo su responsable puede cerrarlo."));

        mvc.perform(get("/api/turnos/actual").header("Authorization", "Bearer " + token("operario3")))
                .andExpect(jsonPath("$.turnoDeHoy.estado").value("ABIERTO"));
    }

    @Test
    void iniciarDosVecesElTurnoDelMismoLoteResponde409_F10() throws Exception {
        String token = token("operario2");
        mvc.perform(post("/api/turnos/inicio").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/turnos/inicio").header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void elWorkerConsultaSuTurnoDeHoy_F10() throws Exception {
        mvc.perform(get("/api/turnos/actual").header("Authorization", "Bearer " + token("operario3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.galponNombre").value("Galpón 3"))
                .andExpect(jsonPath("$.turnoDeHoy.estado").value("ABIERTO"))
                .andExpect(jsonPath("$.turnoDeHoy.responsableNombre").value("Óscar Operario"));
    }

    /** Segregacion de funciones: el administrador supervisa los turnos, no inicia ni cierra los suyos. */
    @Test
    void elAdministradorNoIniciaNiConsultaTurnosPropios_F10() throws Exception {
        String admin = token("admin");
        mvc.perform(get("/api/turnos/actual").header("Authorization", "Bearer " + admin))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/turnos/inicio").header("Authorization", "Bearer " + admin))
                .andExpect(status().isForbidden());
    }

    /** Matriz rol → dashboard de cierres pendientes (permiso VER_TURNOS, solo Administrador). */
    @ParameterizedTest(name = "{0} al ver el dashboard de turnos → HTTP {1}")
    @CsvSource({
            "admin,       200",
            "operario,    403",
            "veterinario, 403",
            "dueno,       403",
            "zootecnista, 403",
            "tecnico,     403",
    })
    void soloElAdministradorVeTodosLosTurnos_F10(String username, int statusEsperado) throws Exception {
        mvc.perform(get("/api/turnos").header("Authorization", "Bearer " + token(username)))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    void elDashboardFiltraLosTurnosPorEstado_F10() throws Exception {
        mvc.perform(get("/api/turnos").param("estado", "BLOQUEADO_ALERTA_SANITARIA")
                        .header("Authorization", "Bearer " + token("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].galponNombre").value("Galpón 2"))
                .andExpect(jsonPath("$[0].responsableNombre").value("Olga Operaria"))
                .andExpect(jsonPath("$[0].alertaId").isNotEmpty());
    }

    /** Matriz rol -> racion del dia (permiso VER_RACION). */
    @ParameterizedTest(name = "{0} al ver la racion -> HTTP {1}")
    @CsvSource({
            "zootecnista, 200",
            "operario,    200",
            "admin,       200",
            "veterinario, 403",
            "dueno,       403",
            "tecnico,     403",
    })
    void soloZootecnistaOperarioYAdministradorVenLaRacion_F06(String username, int statusEsperado) throws Exception {
        mvc.perform(get("/api/racion").header("Authorization", "Bearer " + token(username)))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    void laRacionDelDiaSaleDeLaTablaEstandarParaCadaGalpon_F06() throws Exception {
        mvc.perform(get("/api/racion").header("Authorization", "Bearer " + token("zootecnista")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].galponNombre").value("Galpón 1"))
                .andExpect(jsonPath("$[0].gramosPorAve").value(23.0))
                .andExpect(jsonPath("$[0].estrategia").value("Tabla de nutrición estándar"));
    }

    @Test
    void elOperarioVeLaRacionSugeridaAlConsultarSuTurno_F06() throws Exception {
        mvc.perform(get("/api/turnos/actual").header("Authorization", "Bearer " + token("operario3")))
                .andExpect(status().isOk())
                // Galpon 3: 600 aves - 3 bajas del cierre de ayer = 597 x 17 g
                .andExpect(jsonPath("$.racionSugeridaKg").value(10.1));
    }

    /** Matriz rol -> listado de alertas sanitarias (permiso VER_ALERTAS_SANITARIAS). */
    @ParameterizedTest(name = "{0} al listar alertas -> HTTP {1}")
    @CsvSource({
            "veterinario, 200",
            "dueno,       200",
            "admin,       200",
            "operario,    403",
            "zootecnista, 403",
            "tecnico,     403",
    })
    void soloVeterinarioDuenoYAdministradorVenLasAlertas_F026(String username, int statusEsperado) throws Exception {
        mvc.perform(get("/api/alertas").header("Authorization", "Bearer " + token(username)))
                .andExpect(status().is(statusEsperado));
    }

    /**
     * Matriz rol -> atender una alerta (permiso ATENDER_ALERTAS_SANITARIAS), sobre
     * una alerta inexistente: con permiso llega al caso de uso (404); sin permiso, 403.
     */
    @ParameterizedTest(name = "{0} al atender una alerta -> HTTP {1}")
    @CsvSource({
            "veterinario, 404",
            "admin,       403",
            "dueno,       403",
            "operario,    403",
            "zootecnista, 403",
            "tecnico,     403",
    })
    void soloElVeterinarioAtiendeAlertas_F025(String username, int statusEsperado) throws Exception {
        mvc.perform(post("/api/alertas/no-existe/atencion").header("Authorization", "Bearer " + token(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"diagnostico\":\"x\",\"tratamiento\":\"y\"}"))
                .andExpect(status().is(statusEsperado));
    }

    /** Flujo completo de F-02.5 sobre la alerta de demo del Galpon 2. Cambia los datos de demo: reinicia el contexto. */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void elVeterinarioAtiendeLaAlertaYElTurnoQuedaCerradoConAlerta_F025() throws Exception {
        String token = token("veterinario");
        String cuerpo = "{\"diagnostico\":\"Bronquitis infecciosa\",\"tratamiento\":\"Vacuna de refuerzo\",\"liberarLote\":true}";

        mvc.perform(post("/api/alertas/alerta-demo-t2/atencion").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ATENDIDA"))
                .andExpect(jsonPath("$.estadoTurno").value("CERRADO_CON_ALERTA"))
                .andExpect(jsonPath("$.atendidaPorNombre").value("Valeria Veterinaria"))
                .andExpect(jsonPath("$.galponNombre").value("Galpón 2"));

        mvc.perform(post("/api/alertas/alerta-demo-t2/atencion").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict());
    }

    /** Matriz rol -> registrar un pesaje (permiso REGISTRAR_PESO); el body vacio llega al caso de uso como 400. */
    @ParameterizedTest(name = "{0} al registrar un pesaje -> HTTP {1}")
    @CsvSource({
            "operario,    400",
            "admin,       403",
            "zootecnista, 403",
            "veterinario, 403",
            "dueno,       403",
            "tecnico,     403",
    })
    void soloElOperarioRegistraPesajes_F07(String username, int statusEsperado) throws Exception {
        mvc.perform(post("/api/pesajes").header("Authorization", "Bearer " + token(username))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().is(statusEsperado));
    }

    /** Matriz rol -> revisar un pesaje (permiso REVISAR_PESAJES), sobre un pesaje inexistente. */
    @ParameterizedTest(name = "{0} al revisar un pesaje -> HTTP {1}")
    @CsvSource({
            "zootecnista, 404",
            "admin,       403",
            "operario,    403",
            "veterinario, 403",
            "dueno,       403",
            "tecnico,     403",
    })
    void soloElZootecnistaRevisaPesajes_F07(String username, int statusEsperado) throws Exception {
        mvc.perform(post("/api/pesajes/no-existe/revision").header("Authorization", "Bearer " + token(username))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"aprobarAjuste\":true}"))
                .andExpect(status().is(statusEsperado));
    }

    /** Matriz rol -> ver los pesajes (permiso VER_PESAJES): el administrador los supervisa en solo lectura. */
    @ParameterizedTest(name = "{0} al ver los pesajes -> HTTP {1}")
    @CsvSource({
            "zootecnista, 200",
            "admin,       200",
            "operario,    403",
            "veterinario, 403",
            "dueno,       403",
            "tecnico,     403",
    })
    void zootecnistaYAdministradorVenLosPesajes_F07(String username, int statusEsperado) throws Exception {
        mvc.perform(get("/api/pesajes").header("Authorization", "Bearer " + token(username)))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    void elZootecnistaVeLosPesajesConLosPendientesPrimero_F07() throws Exception {
        mvc.perform(get("/api/pesajes").header("Authorization", "Bearer " + token("zootecnista")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("demo-p2"))
                .andExpect(jsonPath("$[0].galponNombre").value("Galpón 3"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE_REVISION"))
                .andExpect(jsonPath("$[0].fueraDeRango").value(true))
                .andExpect(jsonPath("$[0].desviacionPorcentaje").value(-5.83));
        mvc.perform(get("/api/pesajes").header("Authorization", "Bearer " + token("operario")))
                .andExpect(status().isForbidden());
    }

    @Test
    void elWorkerVeElPesoEstandarYElPesajeDeEstaSemana_F07() throws Exception {
        mvc.perform(get("/api/pesajes/referencia").header("Authorization", "Bearer " + token("operario3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.galponNombre").value("Galpón 3"))
                .andExpect(jsonPath("$.pesoEstandarGramos").value(120.0))
                .andExpect(jsonPath("$.pesajeDeLaSemana.id").value("demo-p2"));
    }

    /** Flujo completo de F-07: registrar fuera de rango, aprobar el ajuste y ver la racion recalculada. */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void unPesajeFueraDeRangoAprobadoRecalculaLaRacion_F07() throws Exception {
        // Galpon 1, semana 3: estandar 190 g y 23 g de racion. 170 g es -10.53%.
        String respuesta = mvc.perform(post("/api/pesajes").header("Authorization", "Bearer " + token("operario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pesoPromedioGramos\":170,\"tamanoMuestra\":30}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_REVISION"))
                .andExpect(jsonPath("$.desviacionPorcentaje").value(-10.53))
                .andExpect(jsonPath("$.registradoPorNombre").value("Pedro Operario"))
                .andReturn().getResponse().getContentAsString();
        String pesajeId = json.readTree(respuesta).get("id").asText();

        mvc.perform(post("/api/pesajes").header("Authorization", "Bearer " + token("operario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pesoPromedioGramos\":190,\"tamanoMuestra\":30}"))
                .andExpect(status().isConflict());

        String zootecnista = token("zootecnista");
        mvc.perform(post("/api/pesajes/" + pesajeId + "/revision").header("Authorization", "Bearer " + zootecnista)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"aprobarAjuste\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("AJUSTE_APROBADO"))
                .andExpect(jsonPath("$.revisadoPorNombre").value("Zoe Zootecnista"));

        mvc.perform(get("/api/racion").header("Authorization", "Bearer " + zootecnista))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].galponNombre").value("Galpón 1"))
                .andExpect(jsonPath("$[0].gramosPorAve").value(25.7))
                .andExpect(jsonPath("$[0].estrategia").value("Ajustada por peso (+11.8%)"));
    }

    /** Matriz rol -> reportar un dano (permiso REPORTAR_DANOS); el body vacio llega al caso de uso como 400. */
    @ParameterizedTest(name = "{0} al reportar un dano -> HTTP {1}")
    @CsvSource({
            "operario,    400",
            "admin,       403",
            "tecnico,     403",
            "veterinario, 403",
            "dueno,       403",
            "zootecnista, 403",
    })
    void soloElOperarioReportaDanos_F08(String username, int statusEsperado) throws Exception {
        mvc.perform(post("/api/danos").header("Authorization", "Bearer " + token(username))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().is(statusEsperado));
    }

    /** Matriz rol -> certificar una orden (permiso ATENDER_ORDENES_MANTENIMIENTO), sobre una orden inexistente. */
    @ParameterizedTest(name = "{0} al certificar una orden -> HTTP {1}")
    @CsvSource({
            "tecnico,     404",
            "admin,       403",
            "operario,    403",
            "veterinario, 403",
            "dueno,       403",
            "zootecnista, 403",
    })
    void soloElTecnicoAtiendeOrdenes_F08(String username, int statusEsperado) throws Exception {
        mvc.perform(post("/api/ordenes-mantenimiento/no-existe/certificacion")
                        .header("Authorization", "Bearer " + token(username))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"notaReparacion\":\"x\"}"))
                .andExpect(status().is(statusEsperado));
    }

    /** Matriz rol -> ver la bandeja de ordenes (permiso VER_ORDENES_MANTENIMIENTO): el administrador supervisa los plazos. */
    @ParameterizedTest(name = "{0} al ver las ordenes -> HTTP {1}")
    @CsvSource({
            "tecnico,     200",
            "admin,       200",
            "operario,    403",
            "veterinario, 403",
            "dueno,       403",
            "zootecnista, 403",
    })
    void tecnicoYAdministradorVenLasOrdenes_F08(String username, int statusEsperado) throws Exception {
        mvc.perform(get("/api/ordenes-mantenimiento").header("Authorization", "Bearer " + token(username)))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    void elTecnicoVeLaOrdenVencidaDelGalpon2_F083() throws Exception {
        mvc.perform(get("/api/ordenes-mantenimiento").header("Authorization", "Bearer " + token("tecnico")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("demo-o1"))
                .andExpect(jsonPath("$[0].galponNombre").value("Galpón 2"))
                .andExpect(jsonPath("$[0].estadoGalpon").value("EN_RIESGO_SANITARIO"))
                .andExpect(jsonPath("$[0].prioridad").value("ALTA"))
                .andExpect(jsonPath("$[0].vencida").value(true));
    }

    /** El galpon del dano sale de la asignacion del operario: aunque envie otro, se registra en el suyo. */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void elDanoSeRegistraEnElGalponAsignadoDelOperario_F082() throws Exception {
        mvc.perform(post("/api/danos").header("Authorization", "Bearer " + token("operario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"galponId\":\"galpon-3\",\"tipoDano\":\"DANO_TECHO\",\"descripcion\":\"Gotera\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.galponNombre").value("Galpón 1"));
    }

    /** Flujo completo de F-08: reportar, el galpon queda en riesgo, el tecnico certifica y vuelve a ACTIVO. */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void reportarYCertificarUnDanoDevuelveElGalponAActivo_F08() throws Exception {
        String respuesta = mvc.perform(post("/api/danos").header("Authorization", "Bearer " + token("operario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoDano\":\"CORTINA_DANADA\",\"descripcion\":\"Cortina lateral rasgada\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.galponNombre").value("Galpón 1"))
                .andExpect(jsonPath("$.estadoGalpon").value("EN_RIESGO_SANITARIO"))
                .andExpect(jsonPath("$.estado").value("ABIERTA"))
                .andExpect(jsonPath("$.prioridad").value("MEDIA"))
                .andExpect(jsonPath("$.minutosRestantes").value(720))
                .andReturn().getResponse().getContentAsString();
        String ordenId = json.readTree(respuesta).get("id").asText();

        String tecnico = token("tecnico");
        mvc.perform(post("/api/ordenes-mantenimiento/" + ordenId + "/inicio").header("Authorization", "Bearer " + tecnico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_REPARACION"))
                .andExpect(jsonPath("$.tecnicoNombre").value("Tomás Técnico"));

        mvc.perform(post("/api/ordenes-mantenimiento/" + ordenId + "/certificacion")
                        .header("Authorization", "Bearer " + tecnico)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"notaReparacion\":\" \"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/ordenes-mantenimiento/" + ordenId + "/certificacion")
                        .header("Authorization", "Bearer " + tecnico)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"notaReparacion\":\"Se cosió la cortina\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERTIFICADA"))
                .andExpect(jsonPath("$.estadoGalpon").value("ACTIVO"));

        mvc.perform(post("/api/ordenes-mantenimiento/" + ordenId + "/certificacion")
                        .header("Authorization", "Bearer " + tecnico)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"notaReparacion\":\"Otra vez\"}"))
                .andExpect(status().isConflict());
    }

    /** F-09.7: la sesion dura 8 horas desde que se emite el token. */
    @Test
    void elTokenDeSesionDuraOchoHoras_F097() throws Exception {
        Jwt jwt = jwtDecoder.decode(token("operario"));

        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(8));
    }

    /** F-09.7: un token vencido, aunque tenga una firma valida, ya no da acceso. */
    @Test
    void unTokenExpiradoResponde401_F097() throws Exception {
        Instant hace9Horas = Instant.now().minus(Duration.ofHours(9));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("avimanager")
                .subject("operario")
                .issuedAt(hace9Horas)
                .expiresAt(hace9Horas.plus(Duration.ofHours(8)))
                .claim("nombre", "Pedro Operario")
                .claim("rol", "OPERARIO")
                .claim("permisos", List.of("CERRAR_TURNO"))
                .build();
        String tokenExpirado = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tokenExpirado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Debes iniciar sesión para continuar"));
    }

    @Test
    void meDevuelveElUsuarioDelToken() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token("dueno")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("dueno"))
                .andExpect(jsonPath("$.rol").value("DUENO"));
    }

    // --- F-11: galpones, lotes y asignacion de workers ---

    /** Matriz rol -> consulta de galpones (permiso VER_LOTES: lo tienen todos los roles). */
    @ParameterizedTest(name = "{0} al ver los galpones -> HTTP 200")
    @CsvSource({"admin", "operario", "veterinario", "dueno", "zootecnista", "tecnico"})
    void todosLosRolesConVER_LOTESVenLosGalpones_F11(String username) throws Exception {
        mvc.perform(get("/api/galpones").header("Authorization", "Bearer " + token(username)))
                .andExpect(status().isOk());
    }

    @Test
    void cadaGalponMuestraSuLoteActivoYSusWorkers_F11() throws Exception {
        mvc.perform(get("/api/galpones").header("Authorization", "Bearer " + token("dueno")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].nombre").value("Galpón 1"))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"))
                .andExpect(jsonPath("$[0].loteActivo.id").value("lote-1"))
                .andExpect(jsonPath("$[0].workers[0].username").value("operario"))
                .andExpect(jsonPath("$[2].workers.length()").value(2))
                .andExpect(jsonPath("$[3].nombre").value("Galpón 4"))
                .andExpect(jsonPath("$[3].loteActivo").isEmpty());
    }

    /**
     * Matriz rol -> gestion de galpones (permiso GESTIONAR_GALPONES): solo el
     * Administrador; los demas roles reciben 403 en cada endpoint de escritura.
     */
    @ParameterizedTest(name = "{0} al gestionar galpones -> HTTP 403")
    @CsvSource({"operario", "veterinario", "dueno", "zootecnista", "tecnico"})
    void soloElAdministradorGestionaGalpones_F11(String username) throws Exception {
        String token = token(username);
        mvc.perform(post("/api/galpones").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Galpón 9\",\"capacidadMaxima\":100}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/galpones/galpon-4/lotes").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"poblacionInicial\":100,\"edadSemanas\":0}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/lotes/lote-1/finalizacion").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/usuarios/operario4/galpon").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"galponId\":\"galpon-4\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/workers").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void elAdministradorRegistraUnGalponYUnLoteDentroDeSuCapacidad_F11() throws Exception {
        String token = token("admin");
        String respuesta = mvc.perform(post("/api/galpones").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Galpón 5\",\"capacidadMaxima\":400}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andReturn().getResponse().getContentAsString();
        String galponId = json.readTree(respuesta).get("id").asText();

        mvc.perform(post("/api/galpones/" + galponId + "/lotes").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"poblacionInicial\":401,\"edadSemanas\":0}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/galpones/" + galponId + "/lotes").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"poblacionInicial\":400,\"edadSemanas\":0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.galponId").value(galponId))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
        mvc.perform(post("/api/galpones/" + galponId + "/lotes").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"poblacionInicial\":100,\"edadSemanas\":0}"))
                .andExpect(status().isConflict());
    }

    @Test
    void unGalponSinNombreResponde400YUnGalponQueNoExisteResponde404_F11() throws Exception {
        String token = token("admin");
        mvc.perform(post("/api/galpones").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"capacidadMaxima\":100}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/galpones/galpon-99/lotes").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"poblacionInicial\":100,\"edadSemanas\":0}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void finalizarElLoteLiberaElGalponParaUnLoteNuevo_F11() throws Exception {
        String token = token("admin");
        mvc.perform(post("/api/lotes/lote-1/finalizacion").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"));
        mvc.perform(post("/api/lotes/lote-1/finalizacion").header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/galpones/galpon-1/lotes").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"poblacionInicial\":550,\"edadSemanas\":0}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void elAdministradorAsignaWorkersSalvoSiTienenUnTurnoAbierto_F11() throws Exception {
        String token = token("admin");
        mvc.perform(get("/api/workers").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
        // operario4 no tiene turno abierto: pasa al Galpon 4
        mvc.perform(put("/api/usuarios/operario4/galpon").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"galponId\":\"galpon-4\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.galponAsignadoId").value("galpon-4"));
        // operario3 tiene abierto el turno de hoy (datos de demo)
        mvc.perform(put("/api/usuarios/operario3/galpon").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"galponId\":\"galpon-4\"}"))
                .andExpect(status().isConflict());
        // solo los operarios se asignan
        mvc.perform(put("/api/usuarios/veterinario/galpon").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"galponId\":\"galpon-4\"}"))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/usuarios/nadie/galpon").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"galponId\":\"galpon-4\"}"))
                .andExpect(status().isNotFound());
    }

    // --- F-03: inventario y stock critico ---

    /** Matriz rol -> inventario (permiso VER_INVENTARIO: Administrador y Dueño). */
    @ParameterizedTest(name = "{0} al ver el inventario -> HTTP {1}")
    @CsvSource({
            "admin,       200",
            "dueno,       200",
            "operario,    403",
            "veterinario, 403",
            "zootecnista, 403",
            "tecnico,     403",
    })
    void soloAdministradorYDuenoVenElInventario_F03(String username, int statusEsperado) throws Exception {
        mvc.perform(get("/api/inventario").header("Authorization", "Bearer " + token(username)))
                .andExpect(status().is(statusEsperado));
    }

    /**
     * De punta a punta: un cierre de turno descuenta el alimento de la bodega. Usa
     * un lote y un worker propios (Galpon 4 y operario4) para no depender del
     * orden en que corren las demas pruebas, que comparten los datos de demo.
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void cerrarUnTurnoDescuentaElAlimentoDeLaBodega_F03() throws Exception {
        String admin = token("admin");
        double stockAntes = json.readTree(mvc.perform(get("/api/inventario").header("Authorization", "Bearer " + admin))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].id").value("insumo-alimento"))
                        .andReturn().getResponse().getContentAsString())
                .get(0).get("stock").asDouble();

        String loteId = json.readTree(mvc.perform(post("/api/galpones/galpon-4/lotes")
                                .header("Authorization", "Bearer " + admin).contentType(MediaType.APPLICATION_JSON)
                                .content("{\"poblacionInicial\":500,\"edadSemanas\":3}"))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString())
                .get("id").asText();
        mvc.perform(put("/api/usuarios/operario4/galpon").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"galponId\":\"galpon-4\"}"))
                .andExpect(status().isOk());

        String operario = token("operario4");
        mvc.perform(post("/api/turnos/inicio").header("Authorization", "Bearer " + operario))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/lotes/" + loteId + "/turnos/cierre").header("Authorization", "Bearer " + operario)
                        .contentType(MediaType.APPLICATION_JSON).content(TURNO_NORMAL))
                .andExpect(status().isOk());

        // El demo arranca en 5.005 kg (50.05%): tras 45 kg queda bajo el 50%, en Stock Critico.
        mvc.perform(get("/api/inventario").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stock").value(stockAntes - 45.0))
                .andExpect(jsonPath("$[0].critico").value(true));
    }

    // --- F-05: Acta de Ajuste de inventario (RN-08) ---

    /** Matriz rol -> registrar Acta de Ajuste (permiso AJUSTAR_INVENTARIO: solo el Administrador). */
    @ParameterizedTest(name = "{0} al registrar un acta de ajuste -> HTTP 403")
    @CsvSource({"operario", "veterinario", "dueno", "zootecnista", "tecnico"})
    void soloElAdministradorRegistraActasDeAjuste_F05(String username) throws Exception {
        mvc.perform(post("/api/inventario/insumo-vacuna/ajustes").header("Authorization", "Bearer " + token(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockContado\":1400,\"justificacion\":\"Conteo físico de la bodega\"}"))
                .andExpect(status().isForbidden());
    }

    /** Matriz rol -> historial de actas (permiso VER_INVENTARIO: Administrador y Dueño). */
    @ParameterizedTest(name = "{0} al ver el historial de actas -> HTTP {1}")
    @CsvSource({
            "admin,       200",
            "dueno,       200",
            "operario,    403",
            "veterinario, 403",
            "zootecnista, 403",
            "tecnico,     403",
    })
    void soloAdministradorYDuenoVenElHistorialDeActas_F05(String username, int statusEsperado) throws Exception {
        mvc.perform(get("/api/inventario/ajustes").header("Authorization", "Bearer " + token(username)))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void elAdministradorRegistraUnActaYElDuenoLaVeEnElHistorial_F05() throws Exception {
        String admin = token("admin");
        mvc.perform(post("/api/inventario/insumo-vacuna/ajustes").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockContado\":1400,\"justificacion\":\"Conteo físico: 100 dosis vencidas\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stockSistema").value(1500.0))
                .andExpect(jsonPath("$.stockContado").value(1400.0))
                .andExpect(jsonPath("$.diferencia").value(-100.0))
                .andExpect(jsonPath("$.administradorUsername").value("admin"))
                .andExpect(jsonPath("$.administradorNombre").value("Ana Administradora"));

        mvc.perform(get("/api/inventario/ajustes").header("Authorization", "Bearer " + token("dueno")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].insumoNombre").value("Vacuna Newcastle"))
                .andExpect(jsonPath("$[0].justificacion").value("Conteo físico: 100 dosis vencidas"));
        mvc.perform(get("/api/inventario").header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$[1].id").value("insumo-vacuna"))
                .andExpect(jsonPath("$[1].stock").value(1400.0));
    }

    @Test
    void unActaSinJustificacionResponde400YUnInsumoQueNoExisteResponde404_F05() throws Exception {
        String admin = token("admin");
        mvc.perform(post("/api/inventario/insumo-vacuna/ajustes").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stockContado\":1400}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/inventario/no-existe/ajustes").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockContado\":10,\"justificacion\":\"Conteo físico de la bodega\"}"))
                .andExpect(status().isNotFound());
    }

    // --- F-04: KPIs, aprobacion de reportes y consolidacion del dia ---

    /** Matriz rol -> dashboard de KPIs (permiso VER_DASHBOARD_KPI: Dueño y Administrador). */
    @ParameterizedTest(name = "{0} al ver los KPIs -> HTTP {1}")
    @CsvSource({
            "admin,       200",
            "dueno,       200",
            "operario,    403",
            "veterinario, 403",
            "zootecnista, 403",
            "tecnico,     403",
    })
    void soloDuenoYAdministradorVenLosKpis_F04(String username, int statusEsperado) throws Exception {
        mvc.perform(get("/api/kpis").header("Authorization", "Bearer " + token(username)))
                .andExpect(status().is(statusEsperado));
        mvc.perform(get("/api/kpis/consolidados").header("Authorization", "Bearer " + token(username)))
                .andExpect(status().is(statusEsperado));
    }

    /** Matriz rol -> aprobar reportes y consolidar (permiso APROBAR_REPORTES: solo el Administrador). */
    @ParameterizedTest(name = "{0} al aprobar o consolidar -> HTTP 403")
    @CsvSource({"operario", "veterinario", "dueno", "zootecnista", "tecnico"})
    void soloElAdministradorApruebaYConsolida_F04(String username) throws Exception {
        String token = token(username);
        mvc.perform(post("/api/turnos/demo-t1/aprobacion").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/kpis/consolidacion").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"fecha\":\"2026-01-01\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void losKpisTraenLasTresEstrategiasPorCadaLoteActivo_F04() throws Exception {
        mvc.perform(get("/api/kpis").header("Authorization", "Bearer " + token("dueno")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].galponNombre").value("Galpón 1"))
                .andExpect(jsonPath("$[0].kpis.length()").value(3))
                .andExpect(jsonPath("$[0].kpis[0].clave").value("CONVERSION_ALIMENTICIA"))
                .andExpect(jsonPath("$[0].kpis[1].clave").value("MORTALIDAD_ACUMULADA"))
                .andExpect(jsonPath("$[0].kpis[2].clave").value("PRODUCCION_TOTAL"));
    }

    @Test
    void unTurnoAbiertoNoSeApruebaResponde409_F04() throws Exception {
        // demo-t4: turno de hoy de operario3, todavia ABIERTO
        mvc.perform(post("/api/turnos/demo-t4/aprobacion").header("Authorization", "Bearer " + token("admin")))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/turnos/no-existe/aprobacion").header("Authorization", "Bearer " + token("admin")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void elAdministradorApruebaYNoConsolidaMientrasQuedenTurnosSinAprobar_F04() throws Exception {
        String admin = token("admin");
        String fechaDemoT1 = json.readTree(mvc.perform(post("/api/turnos/demo-t1/aprobacion")
                                .header("Authorization", "Bearer " + admin))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.estado").value("APROBADO"))
                        .andExpect(jsonPath("$.aprobadoPor").value("admin"))
                        .andReturn().getResponse().getContentAsString())
                .get("fecha").asText();

        // Ese dia (ayer en los datos de demo) demo-t2 sigue bloqueado por alerta y demo-t3 sin aprobar.
        mvc.perform(post("/api/kpis/consolidacion").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"fecha\":\"" + fechaDemoT1 + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("sin aprobar")));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void unDiaSinTurnosPendientesSeConsolidaUnaSolaVez_F04() throws Exception {
        String admin = token("admin");
        String hace10Dias = java.time.LocalDate.now().minusDays(10).toString();
        String cuerpo = "{\"fecha\":\"" + hace10Dias + "\",\"conteoFisico\":" + conteoQueCuadra(admin) + "}";
        mvc.perform(post("/api/kpis/consolidacion").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consolidadoPorNombre").value("Ana Administradora"))
                .andExpect(jsonPath("$.lotes.length()").value(3))
                .andExpect(jsonPath("$.conteoFisico['insumo-alimento']").value(5005.0));
        mvc.perform(post("/api/kpis/consolidacion").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/kpis/consolidados").header("Authorization", "Bearer " + token("dueno")))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fecha").value(hace10Dias));
    }

    /** RN-06: si el conteo fisico no cuadra, no se consolida hasta registrar el Acta de Ajuste. */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void conElInventarioDescuadradoNoSeConsolidaHastaElActaDeAjuste_RN06() throws Exception {
        String admin = token("admin");
        String hace10Dias = java.time.LocalDate.now().minusDays(10).toString();
        String conteoDescuadrado = conteoQueCuadra(admin).replace("\"insumo-alimento\":5005.0", "\"insumo-alimento\":4950.0");
        String cuerpo = "{\"fecha\":\"" + hace10Dias + "\",\"conteoFisico\":" + conteoDescuadrado + "}";

        mvc.perform(post("/api/kpis/consolidacion").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("Acta de Ajuste")))
                .andExpect(jsonPath("$.diferencias.length()").value(1))
                .andExpect(jsonPath("$.diferencias[0].insumoId").value("insumo-alimento"))
                .andExpect(jsonPath("$.diferencias[0].diferencia").value(-55.0));

        mvc.perform(post("/api/inventario/insumo-alimento/ajustes").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockContado\":4950,\"justificacion\":\"Conteo fisico: 2 bultos rotos por humedad\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/kpis/consolidacion").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());
    }

    /** Arma el conteo fisico igual al stock actual de cada insumo: {"insumo-alimento":5005.0,...}. */
    private String conteoQueCuadra(String token) throws Exception {
        JsonNode insumos = json.readTree(mvc.perform(get("/api/inventario").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString());
        java.util.Map<String, Double> conteo = new java.util.LinkedHashMap<>();
        insumos.forEach(i -> conteo.put(i.get("id").asText(), i.get("stock").asDouble()));
        return json.writeValueAsString(conteo);
    }

    private String token(String username) throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"contrasena\":\"" + username + "123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode nodo = json.readTree(respuesta);
        return nodo.get("token").asText();
    }
}
