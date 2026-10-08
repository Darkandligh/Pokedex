package co.edu.fet.pokedex.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de los endpoints con MockMvc sobre el perfil de datos fijos (N.º 0001 a 0009).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("datosfijos")
class PokemonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fichaPorNumero() throws Exception {
        mockMvc.perform(get("/api/pokemon/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(6))
                .andExpect(jsonPath("$.nombre").value("Charizard"))
                .andExpect(jsonPath("$.tipos[*].nombre").value(org.hamcrest.Matchers.contains("Fuego", "Volador")))
                .andExpect(jsonPath("$.debilidades", hasSize(3)))
                .andExpect(jsonPath("$.cadenaEvolutiva", hasSize(3)));
    }

    @Test
    void fichaPorNombreSinDistinguirMayusculas() throws Exception {
        mockMvc.perform(get("/api/pokemon/nombre/bULBASAUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(1))
                .andExpect(jsonPath("$.nombre").value("Bulbasaur"));
    }

    @Test
    void numeroInexistenteDevuelve404ConMensaje() throws Exception {
        mockMvc.perform(get("/api/pokemon/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("Pokémon no encontrado"));
    }

    @Test
    void nombreInexistenteDevuelve404ConMensaje() throws Exception {
        mockMvc.perform(get("/api/pokemon/nombre/missingno"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Pokémon no encontrado"));
    }

    @Test
    void numeroNoNumericoDevuelve404() throws Exception {
        mockMvc.perform(get("/api/pokemon/abc"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Pokémon no encontrado"));
    }
}
