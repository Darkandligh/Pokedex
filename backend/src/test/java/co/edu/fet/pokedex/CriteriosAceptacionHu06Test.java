package co.edu.fet.pokedex;

import co.edu.fet.pokedex.repository.NoticiaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-06 · Ver noticias recientes en la portada (SCRUM-14), con las noticias de ejemplo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("datosfijos")
class CriteriosAceptacionHu06Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoticiaRepository noticiaRepository;

    @Test
    @DisplayName("CA1: las noticias vienen de la más reciente a la más antigua, con título, resumen, imagen y fecha")
    void ca1NoticiasRecientes() throws Exception {
        mockMvc.perform(get("/api/noticias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[*].fecha").value(contains(
                        "2026-10-15", "2026-10-13", "2026-10-08", "2026-10-08", "2026-10-01", "2026-09-24")))
                .andExpect(jsonPath("$[0].titulo").value("Filtra la Pokédex por tipo"))
                .andExpect(jsonPath("$[0].resumen").value(containsString("18 tipos")))
                .andExpect(jsonPath("$[0].imagenUrl").value(containsString("official-artwork/6.png")))
                .andExpect(jsonPath("$[0].id").isNumber());
    }

    @Test
    @DisplayName("CA1: con limite se piden menos noticias (entre 1 y 20)")
    void ca1Limite() throws Exception {
        mockMvc.perform(get("/api/noticias").param("limite", "3"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[2].fecha").value("2026-10-08"));
        mockMvc.perform(get("/api/noticias").param("limite", "0"))
                .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/noticias").param("limite", "500"))
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    @Transactional
    @DisplayName("CA3: sin noticias la API devuelve una lista vacía (el frontend muestra «No hay noticias por ahora»)")
    void ca3SinNoticias() throws Exception {
        noticiaRepository.deleteAll();

        mockMvc.perform(get("/api/noticias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Las noticias son públicas: no piden iniciar sesión")
    void noticiasPublicas() throws Exception {
        mockMvc.perform(get("/api/noticias")).andExpect(status().isOk());
    }
}
