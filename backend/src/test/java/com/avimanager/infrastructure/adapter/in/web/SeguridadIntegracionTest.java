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
    void operarioPuedeCerrarTurno() throws Exception {
        mvc.perform(post(CIERRE).header("Authorization", "Bearer " + token("operario"))
                        .contentType(MediaType.APPLICATION_JSON).content(TURNO_NORMAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("TURNO_CERRADO"));
    }

    /** Matriz rol → resultado esperado al intentar cerrar un turno (permiso CERRAR_TURNO). */
    @ParameterizedTest(name = "{0} al cerrar turno → HTTP {1}")
    @CsvSource({
            "operario,    200",
            "admin,       200",
            "veterinario, 403",
            "dueno,       403",
            "zootecnista, 403",
            "tecnico,     403",
    })
    void soloLosRolesConPermisoCERRAR_TURNOPuedenCerrarTurno(String username, int statusEsperado) throws Exception {
        mvc.perform(post(CIERRE).header("Authorization", "Bearer " + token(username))
                        .contentType(MediaType.APPLICATION_JSON).content(TURNO_NORMAL))
                .andExpect(status().is(statusEsperado));
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
