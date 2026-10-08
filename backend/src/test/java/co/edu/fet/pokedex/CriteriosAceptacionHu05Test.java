package co.edu.fet.pokedex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-05 · Navegar al Pokémon anterior o siguiente (SCRUM-13), sobre el perfil de datos fijos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("datosfijos")
class CriteriosAceptacionHu05Test {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("CA1: en la ficha del N.º 0002, «anterior» es el 0001 y «siguiente» el 0003")
    void ca1AnteriorYSiguiente() throws Exception {
        mockMvc.perform(get("/api/pokemon/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anterior").value(1))
                .andExpect(jsonPath("$.siguiente").value(3));
    }

    @Test
    @DisplayName("En los extremos de la Pokédex no hay anterior (N.º 0001) ni siguiente (último)")
    void extremos() throws Exception {
        mockMvc.perform(get("/api/pokemon/1"))
                .andExpect(jsonPath("$.anterior").value(nullValue()))
                .andExpect(jsonPath("$.siguiente").value(2));
        mockMvc.perform(get("/api/pokemon/9"))
                .andExpect(jsonPath("$.anterior").value(8))
                .andExpect(jsonPath("$.siguiente").value(nullValue()));
    }

    @Test
    @DisplayName("La ficha por nombre también trae anterior y siguiente")
    void fichaPorNombre() throws Exception {
        mockMvc.perform(get("/api/pokemon/nombre/ivysaur"))
                .andExpect(jsonPath("$.anterior").value(1))
                .andExpect(jsonPath("$.siguiente").value(3));
    }
}
