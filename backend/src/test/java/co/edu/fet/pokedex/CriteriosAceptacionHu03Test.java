package co.edu.fet.pokedex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-03 · Buscar un Pokémon por nombre o número (SCRUM-11), sobre el perfil de datos fijos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("datosfijos")
class CriteriosAceptacionHu03Test {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("CA1: buscar por nombre parcial, sin distinguir mayúsculas, devuelve los que coinciden")
    void ca1BuscarPorNombre() throws Exception {
        mockMvc.perform(get("/api/pokemon").param("q", "SAUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre").value(contains("Bulbasaur", "Ivysaur", "Venusaur")));
        mockMvc.perform(get("/api/pokemon").param("q", "char"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre").value(contains("Charmander", "Charmeleon", "Charizard")));
    }

    @ParameterizedTest(name = "q={0}")
    @ValueSource(strings = {"1", "001", "0001"})
    @DisplayName("CA1: buscar por número acepta 1, 001 y 0001")
    void ca1BuscarPorNumero(String busqueda) throws Exception {
        mockMvc.perform(get("/api/pokemon").param("q", busqueda))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Bulbasaur"));
    }

    @ParameterizedTest(name = "q={0}")
    @ValueSource(strings = {"pikachu", "9999", "xyz"})
    @DisplayName("CA2: sin coincidencias la API devuelve una lista vacía (el frontend muestra «No se encontraron Pokémon»)")
    void ca2SinCoincidencias(String busqueda) throws Exception {
        mockMvc.perform(get("/api/pokemon").param("q", busqueda))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
