package co.edu.fet.pokedex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-13 · Registrar, editar y eliminar Pokémon (SCRUM-21), sobre el perfil de datos fijos.
 * Cada prueba corre en una transacción que se deshace al terminar, así no afecta a las demás.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("datosfijos")
@Transactional
class CriteriosAceptacionHu13Test {

    /** Credenciales del administrador por defecto (application.properties). */
    private static final RequestPostProcessor ADMIN = httpBasic("admin", "pokedex2026");

    @Autowired
    private MockMvc mockMvc;

    /** Formulario válido con el número, el nombre y los tipos de cada prueba. */
    private static String formulario(Integer numero, String nombre, String tipos) {
        return """
                {
                  "numero": %s,
                  "nombre": "%s",
                  "descripcion": "Almacena electricidad en las mejillas.",
                  "imagenUrl": "",
                  "altura": 0.4,
                  "peso": 6.0,
                  "genero": "Macho y hembra",
                  "categoria": "Ratón",
                  "habilidad": "Electricidad Estática",
                  "tipos": [%s],
                  "estadisticas": {"ps": 35, "ataque": 55, "defensa": 40,
                                   "ataqueEspecial": 50, "defensaEspecial": 50, "velocidad": 90}
                }
                """.formatted(numero, nombre, tipos);
    }

    @Test
    @DisplayName("CA1: registrar un Pokémon lo agrega a la Pokédex con sus debilidades calculadas")
    void ca1Registrar() throws Exception {
        mockMvc.perform(post("/api/pokemon").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content(formulario(25, "Pikachu", "\"electric\"")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/pokemon/25")))
                .andExpect(jsonPath("$.numero").value(25))
                .andExpect(jsonPath("$.nombre").value("Pikachu"))
                .andExpect(jsonPath("$.imagenUrl").value(endsWith("official-artwork/25.png")))
                .andExpect(jsonPath("$.tipos[*].nombre").value(contains("Eléctrico")))
                .andExpect(jsonPath("$.debilidades[*].nombre").value(contains("Tierra")))
                .andExpect(jsonPath("$.estadisticas.velocidad").value(90))
                .andExpect(jsonPath("$.anterior").value(9));

        mockMvc.perform(get("/api/pokemon"))
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[9].nombre").value("Pikachu"));
    }

    @Test
    @DisplayName("CA2: editar un Pokémon cambia sus datos y recalcula las debilidades")
    void ca2Editar() throws Exception {
        mockMvc.perform(put("/api/pokemon/4").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content(formulario(null, "Charmander Azul", "\"water\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(4))
                .andExpect(jsonPath("$.nombre").value("Charmander Azul"))
                .andExpect(jsonPath("$.tipos[*].clave").value(contains("water")))
                .andExpect(jsonPath("$.debilidades[*].clave").value(contains("grass", "electric")));

        mockMvc.perform(get("/api/pokemon/4"))
                .andExpect(jsonPath("$.nombre").value("Charmander Azul"))
                .andExpect(jsonPath("$.estadisticas.ps").value(35))
                // La cadena evolutiva se conserva al editar
                .andExpect(jsonPath("$.cadenaEvolutiva", hasSize(3)));
    }

    @Test
    @DisplayName("CA3: los datos inválidos se rechazan con el mensaje de cada campo")
    void ca3ValidarCampos() throws Exception {
        String invalido = """
                {"numero": 0, "nombre": " ", "imagenUrl": "foto.png", "altura": 0, "peso": -1,
                 "genero": "Otro", "categoria": "", "habilidad": "", "tipos": [],
                 "estadisticas": {"ps": 0, "ataque": 300, "defensa": 40,
                                  "ataqueEspecial": 50, "defensaEspecial": 50, "velocidad": 90}}
                """;
        mockMvc.perform(post("/api/pokemon").with(ADMIN).contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("Datos inválidos"))
                .andExpect(jsonPath("$.errores.numero").value("El número debe estar entre 1 y 9999"))
                .andExpect(jsonPath("$.errores.nombre").value("El nombre es obligatorio"))
                .andExpect(jsonPath("$.errores.imagenUrl").exists())
                .andExpect(jsonPath("$.errores.altura").exists())
                .andExpect(jsonPath("$.errores.peso").exists())
                .andExpect(jsonPath("$.errores.genero").exists())
                .andExpect(jsonPath("$.errores.tipos").value("Elige uno o dos tipos"))
                .andExpect(jsonPath("$.errores['estadisticas.ps']").value("Los PS deben estar entre 1 y 255"))
                .andExpect(jsonPath("$.errores['estadisticas.ataque']").value("El ataque debe estar entre 1 y 255"));
    }

    @Test
    @DisplayName("CA3: no se aceptan tipos desconocidos ni repetidos, ni registrar sin número")
    void ca3ValidarTiposYNumero() throws Exception {
        mockMvc.perform(post("/api/pokemon").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content(formulario(25, "Pikachu", "\"luz\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.tipos").value("Tipo desconocido: luz"));
        mockMvc.perform(post("/api/pokemon").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content(formulario(25, "Pikachu", "\"electric\", \"electric\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.tipos").value("Los tipos no se pueden repetir"));
        mockMvc.perform(post("/api/pokemon").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content(formulario(null, "Pikachu", "\"electric\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.numero").value("El número es obligatorio"));
        mockMvc.perform(put("/api/pokemon/4").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content(formulario(40, "Charmander", "\"fire\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.numero").value("El número no se puede cambiar"));
    }

    @Test
    @DisplayName("CA3: no se repiten números ni nombres, y no se edita un Pokémon que no existe")
    void ca3NumeroYNombreUnicos() throws Exception {
        mockMvc.perform(post("/api/pokemon").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content(formulario(1, "Pikachu", "\"electric\"")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe un Pokémon con el N.º 0001"));
        mockMvc.perform(post("/api/pokemon").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content(formulario(25, "squirtle", "\"electric\"")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe un Pokémon llamado Squirtle"));
        mockMvc.perform(put("/api/pokemon/9999").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content(formulario(null, "Pikachu", "\"electric\"")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Pokémon no encontrado"));
    }

    @Test
    @DisplayName("CA4: eliminar un Pokémon lo quita de la Pokédex y de los enlaces de su cadena evolutiva")
    void ca4Eliminar() throws Exception {
        mockMvc.perform(delete("/api/pokemon/7").with(ADMIN))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/pokemon/7"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/pokemon"))
                .andExpect(jsonPath("$", hasSize(8)));
        // Wartortle sigue mostrando a Squirtle en su cadena, pero sin enlace
        mockMvc.perform(get("/api/pokemon/8"))
                .andExpect(jsonPath("$.anterior").value(6))
                .andExpect(jsonPath("$.cadenaEvolutiva[0].numero").value(7))
                .andExpect(jsonPath("$.cadenaEvolutiva[0].disponible").value(false));
        mockMvc.perform(delete("/api/pokemon/7").with(ADMIN))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CA5: sin iniciar sesión como administrador no se puede registrar, editar ni eliminar")
    void ca5SinSesionSeNiegaElAcceso() throws Exception {
        mockMvc.perform(post("/api/pokemon").contentType(MediaType.APPLICATION_JSON)
                        .content(formulario(25, "Pikachu", "\"electric\"")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"))
                .andExpect(jsonPath("$.estado").value(401))
                .andExpect(jsonPath("$.mensaje").value("Debes iniciar sesión como administrador"));
        mockMvc.perform(put("/api/pokemon/4").contentType(MediaType.APPLICATION_JSON)
                        .content(formulario(null, "Charmander", "\"fire\"")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/pokemon/4"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/pokemon/4").with(httpBasic("admin", "clave-equivocada")))
                .andExpect(status().isUnauthorized());

        // Nada cambió y las consultas siguen siendo públicas
        mockMvc.perform(get("/api/pokemon/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Charmander"));
    }

    @Test
    @DisplayName("CA5: un usuario que no es administrador recibe 403")
    void ca5UsuarioSinRolAdministrador() throws Exception {
        mockMvc.perform(delete("/api/pokemon/4").with(user("ash").roles("ENTRENADOR")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.estado").value(403))
                .andExpect(jsonPath("$.mensaje").value("Solo un administrador puede hacer esto"));
        mockMvc.perform(get("/api/pokemon/4"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("El inicio de sesión del administrador se comprueba en /api/admin/sesion")
    void sesionDelAdministrador() throws Exception {
        mockMvc.perform(get("/api/admin/sesion").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("admin"));
        mockMvc.perform(get("/api/admin/sesion").with(httpBasic("admin", "otra")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/sesion"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("El frontend (ionic serve) puede enviar las credenciales en una petición entre orígenes")
    void corsPermiteEditarDesdeElFrontend() throws Exception {
        mockMvc.perform(options("/api/pokemon/4")
                        .header("Origin", "http://localhost:8100")
                        .header("Access-Control-Request-Method", "DELETE")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8100"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("DELETE")));
    }

    @Test
    @DisplayName("Los tipos para el formulario vienen en el orden oficial")
    void tiposEnOrdenOficial() throws Exception {
        mockMvc.perform(get("/api/tipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].clave").value("flying"))
                .andExpect(jsonPath("$[*].nombre").value(hasItem("Eléctrico")));
    }
}
