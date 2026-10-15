package co.edu.fet.pokedex;

import org.junit.jupiter.api.DisplayName;
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
 * HU-04 · Filtrar Pokémon por tipo (SCRUM-12), sobre el perfil de datos fijos (N.º 0001 a 0009).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("datosfijos")
class CriteriosAceptacionHu04Test {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("CA1: al elegir un tipo se ven solo los Pokémon de ese tipo, en orden de número")
    void ca1FiltrarPorTipo() throws Exception {
        mockMvc.perform(get("/api/pokemon").param("tipo", "fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre").value(contains("Charmander", "Charmeleon", "Charizard")));
        mockMvc.perform(get("/api/pokemon").param("tipo", "water"))
                .andExpect(jsonPath("$[*].nombre").value(contains("Squirtle", "Wartortle", "Blastoise")));
    }

    @Test
    @DisplayName("CA1: el tipo secundario también cuenta y no se distinguen mayúsculas")
    void ca1TipoSecundario() throws Exception {
        mockMvc.perform(get("/api/pokemon").param("tipo", "POISON"))
                .andExpect(jsonPath("$[*].nombre").value(contains("Bulbasaur", "Ivysaur", "Venusaur")));
        mockMvc.perform(get("/api/pokemon").param("tipo", "flying"))
                .andExpect(jsonPath("$[*].nombre").value(contains("Charizard")));
    }

    @Test
    @DisplayName("CA2: el filtro por tipo se combina con la búsqueda por nombre o número")
    void ca2CombinarConBusqueda() throws Exception {
        mockMvc.perform(get("/api/pokemon").param("tipo", "flying").param("q", "char"))
                .andExpect(jsonPath("$[*].nombre").value(contains("Charizard")));
        mockMvc.perform(get("/api/pokemon").param("tipo", "fire").param("q", "0004"))
                .andExpect(jsonPath("$[*].nombre").value(contains("Charmander")));
        // Sin coincidencias: lista vacía (el frontend muestra «No se encontraron Pokémon»)
        mockMvc.perform(get("/api/pokemon").param("tipo", "water").param("q", "char"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/pokemon").param("tipo", "electric"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("CA3: sin tipo (o vacío) se ve el listado completo")
    void ca3SinFiltro() throws Exception {
        mockMvc.perform(get("/api/pokemon").param("tipo", ""))
                .andExpect(jsonPath("$", hasSize(9)));
        mockMvc.perform(get("/api/pokemon"))
                .andExpect(jsonPath("$", hasSize(9)));
    }

    @Test
    @DisplayName("Un tipo que no existe no devuelve Pokémon")
    void tipoDesconocido() throws Exception {
        mockMvc.perform(get("/api/pokemon").param("tipo", "luz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
