package co.edu.fet.pokedex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-02 · Ver el listado de la Pokédex (SCRUM-10), sobre el perfil de datos fijos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("datosfijos")
class CriteriosAceptacionHu02Test {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("CA1: el listado viene ordenado por número con imagen, nombre y tipos")
    void ca1ListadoOrdenado() throws Exception {
        mockMvc.perform(get("/api/pokemon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(9)))
                .andExpect(jsonPath("$[*].numero").value(contains(1, 2, 3, 4, 5, 6, 7, 8, 9)))
                .andExpect(jsonPath("$[0].nombre").value("Bulbasaur"))
                .andExpect(jsonPath("$[0].imagenUrl").value(containsString("official-artwork/1.png")))
                .andExpect(jsonPath("$[0].tipos[*].nombre").value(contains("Planta", "Veneno")))
                .andExpect(jsonPath("$[5].tipos[*].nombre").value(contains("Fuego", "Volador")));
    }

    @Test
    @DisplayName("CA2: cada Pokémon del listado tiene su ficha disponible")
    void ca2CadaPokemonDelListadoAbreSuFicha() throws Exception {
        for (int numero = 1; numero <= 9; numero++) {
            mockMvc.perform(get("/api/pokemon/" + numero))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.numero").value(numero));
        }
    }
}
