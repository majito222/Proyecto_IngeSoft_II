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
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
            "admin,       404",
            "veterinario, 403",
            "dueno,       403",
            "zootecnista, 403",
            "tecnico,     403",
    })
    void soloLosRolesConPermisoCERRAR_TURNOPuedenCerrarTurno(String username, int statusEsperado) throws Exception {
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

    @Test
    void sinGalponAsignadoNoHayTurnoQueConsultar_F10() throws Exception {
        mvc.perform(get("/api/turnos/actual").header("Authorization", "Bearer " + token("admin")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("galpón asignado")));
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

    @Test
    void meDevuelveElUsuarioDelToken() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token("dueno")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("dueno"))
                .andExpect(jsonPath("$.rol").value("DUENO"));
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
