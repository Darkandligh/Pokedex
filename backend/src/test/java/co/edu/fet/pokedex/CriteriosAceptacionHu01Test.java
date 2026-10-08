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
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-01 · Ver la ficha de un Pokémon (SCRUM-9): un caso por criterio de aceptación,
 * sobre el perfil de datos fijos (N.º 0001 a 0009).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("datosfijos")
class CriteriosAceptacionHu01Test {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("CA1: la ficha tiene número, nombre, imagen y descripción")
    void ca1NumeroNombreImagenDescripcion() throws Exception {
        mockMvc.perform(get("/api/pokemon/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(1))
                .andExpect(jsonPath("$.nombre").value("Bulbasaur"))
                .andExpect(jsonPath("$.imagenUrl").value(containsString("official-artwork/1.png")))
                .andExpect(jsonPath("$.descripcion").value(containsString("semilla")));
    }

    @Test
    @DisplayName("CA2: la ficha tiene altura, peso, género, categoría, habilidad, tipos y debilidades")
    void ca2Caracteristicas() throws Exception {
        mockMvc.perform(get("/api/pokemon/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.altura").value(0.7))
                .andExpect(jsonPath("$.peso").value(6.9))
                .andExpect(jsonPath("$.genero").value("Macho y hembra"))
                .andExpect(jsonPath("$.categoria").value("Semilla"))
                .andExpect(jsonPath("$.habilidad").value("Espesura"))
                .andExpect(jsonPath("$.tipos[*].nombre").value(contains("Planta", "Veneno")))
                .andExpect(jsonPath("$.debilidades[*].nombre").value(contains("Volador", "Fuego", "Psíquico", "Hielo")));
    }

    @Test
    @DisplayName("CA3: la ficha tiene las 6 estadísticas base y la cadena evolutiva con cada Pokémon enlazable")
    void ca3EstadisticasYCadena() throws Exception {
        mockMvc.perform(get("/api/pokemon/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadisticas.ps").value(45))
                .andExpect(jsonPath("$.estadisticas.ataque").value(49))
                .andExpect(jsonPath("$.estadisticas.defensa").value(49))
                .andExpect(jsonPath("$.estadisticas.ataqueEspecial").value(65))
                .andExpect(jsonPath("$.estadisticas.defensaEspecial").value(65))
                .andExpect(jsonPath("$.estadisticas.velocidad").value(45))
                .andExpect(jsonPath("$.cadenaEvolutiva", hasSize(3)))
                .andExpect(jsonPath("$.cadenaEvolutiva[*].nombre").value(contains("Bulbasaur", "Ivysaur", "Venusaur")))
                .andExpect(jsonPath("$.cadenaEvolutiva[*].disponible").value(contains(true, true, true)))
                .andExpect(jsonPath("$.cadenaEvolutiva[2].imagenUrl").value(notNullValue()));

        // Cada eslabón de la cadena abre su propia ficha
        for (int numero : new int[]{1, 2, 3}) {
            mockMvc.perform(get("/api/pokemon/" + numero))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.numero").value(numero));
        }
    }

    @Test
    @DisplayName("CA4: si el Pokémon no existe, la API responde 404 «Pokémon no encontrado»")
    void ca4NoEncontrado() throws Exception {
        mockMvc.perform(get("/api/pokemon/150"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Pokémon no encontrado"));
        mockMvc.perform(get("/api/pokemon/nombre/pikachu"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Pokémon no encontrado"));
    }
}
