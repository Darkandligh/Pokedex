package co.edu.fet.pokedex.importacion;

import co.edu.fet.pokedex.config.ImportacionProperties;
import co.edu.fet.pokedex.entity.Evolucion;
import co.edu.fet.pokedex.entity.Pokemon;
import co.edu.fet.pokedex.entity.Tipo;
import co.edu.fet.pokedex.importacion.CalculadorDebilidades.RelacionesDano;
import co.edu.fet.pokedex.importacion.ConversorPokeApi.EslabonCadena;
import co.edu.fet.pokedex.repository.EvolucionRepository;
import co.edu.fet.pokedex.repository.PokemonRepository;
import co.edu.fet.pokedex.repository.TipoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 * Importa los Pokémon desde PokéAPI a nuestra base de datos al arrancar.
 *
 * - Solo consulta los Pokémon que todavía no están en la base, así no repite la
 *   importación (y si una importación anterior quedó a medias, la completa).
 * - Si PokéAPI no responde, registra el problema y la aplicación arranca con lo que ya haya.
 */
@Component
@ConditionalOnProperty(prefix = "pokedex.importacion", name = "habilitada", havingValue = "true", matchIfMissing = true)
public class ImportadorPokeApi implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ImportadorPokeApi.class);
    private static final int MAXIMO_FALLOS_SEGUIDOS = 3;

    private final PokeApiCliente cliente;
    private final ImportacionProperties propiedades;
    private final PokemonRepository pokemonRepository;
    private final TipoRepository tipoRepository;
    private final EvolucionRepository evolucionRepository;

    // Cachés en memoria para no repetir peticiones durante la importación
    private final Map<String, Tipo> tipos = new HashMap<>();
    private final Map<String, RelacionesDano> relacionesPorTipo = new HashMap<>();
    private final Map<String, String> habilidades = new HashMap<>();

    public ImportadorPokeApi(PokeApiCliente cliente, ImportacionProperties propiedades,
                             PokemonRepository pokemonRepository, TipoRepository tipoRepository,
                             EvolucionRepository evolucionRepository) {
        this.cliente = cliente;
        this.propiedades = propiedades;
        this.pokemonRepository = pokemonRepository;
        this.tipoRepository = tipoRepository;
        this.evolucionRepository = evolucionRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        int cantidad = propiedades.cantidad();
        long existentes = pokemonRepository.count();
        if (existentes >= cantidad) {
            log.info("La base ya tiene {} Pokémon; no se consulta PokéAPI.", existentes);
            return;
        }

        log.info("Importando Pokémon desde PokéAPI ({} en la base, objetivo {})...", existentes, cantidad);
        try {
            cargarTipos();
        } catch (RestClientException e) {
            log.warn("PokéAPI no responde ({}). La aplicación arranca con los {} Pokémon que ya hay en la base.",
                    e.getMessage(), existentes);
            return;
        }

        int importados = 0;
        int fallosSeguidos = 0;
        for (int numero = 1; numero <= cantidad; numero++) {
            if (pokemonRepository.existsById(numero)) {
                continue;
            }
            try {
                importarPokemon(numero);
                importados++;
                fallosSeguidos = 0;
                if (importados % 25 == 0) {
                    log.info("  {} Pokémon importados (último: N.º {})", importados, numero);
                }
            } catch (RestClientException | IllegalArgumentException e) {
                fallosSeguidos++;
                log.warn("No se pudo importar el Pokémon N.º {}: {}", numero, e.getMessage());
                if (fallosSeguidos >= MAXIMO_FALLOS_SEGUIDOS) {
                    log.warn("PokéAPI dejó de responder; se detiene la importación. Se reintentará al próximo arranque.");
                    break;
                }
            }
        }
        log.info("Importación terminada: {} nuevos, {} en total.", importados, pokemonRepository.count());
    }

    /** Carga los 18 tipos con su nombre en español y sus relaciones de daño. */
    private void cargarTipos() {
        for (String clave : CalculadorDebilidades.TIPOS_OFICIALES) {
            JsonNode json = cliente.obtenerTipo(clave);
            String nombre = ConversorPokeApi.textoEnEspanol(json.path("names"), "name")
                    .orElse(ConversorPokeApi.nombreLegible(clave));
            Tipo tipo = tipoRepository.findByClave(clave)
                    .orElseGet(() -> tipoRepository.save(new Tipo(clave, nombre)));
            tipos.put(clave, tipo);

            JsonNode relaciones = json.path("damage_relations");
            relacionesPorTipo.put(clave, new RelacionesDano(
                    nombres(relaciones.path("double_damage_from")),
                    nombres(relaciones.path("half_damage_from")),
                    nombres(relaciones.path("no_damage_from"))));
        }
    }

    private void importarPokemon(int numero) {
        JsonNode datos = cliente.obtenerPokemon(numero);
        JsonNode especie = cliente.obtenerEspecie(numero);

        String nombre = ConversorPokeApi.textoEnEspanol(especie.path("names"), "name")
                .orElse(ConversorPokeApi.nombreLegible(datos.path("name").asText()));
        Pokemon pokemon = new Pokemon(numero, nombre);
        pokemon.setDescripcion(ConversorPokeApi.descripcion(especie));
        pokemon.setCategoria(ConversorPokeApi.categoria(especie));
        pokemon.setGenero(ConversorPokeApi.genero(especie.path("gender_rate").asInt()));
        // PokéAPI entrega la altura en decímetros y el peso en hectogramos
        pokemon.setAltura(datos.path("height").asInt() / 10.0);
        pokemon.setPeso(datos.path("weight").asInt() / 10.0);
        pokemon.setImagenUrl(imagen(datos, numero));
        pokemon.setHabilidad(habilidadPrincipal(datos));
        asignarEstadisticas(pokemon, datos.path("stats"));

        List<String> clavesTipos = ordenadosPorSlot(datos.path("types")).stream()
                .map(tipo -> tipo.path("type").path("name").asText())
                .toList();
        pokemon.setTipos(clavesTipos.stream().map(this::tipo).toList());
        List<RelacionesDano> relaciones = clavesTipos.stream().map(relacionesPorTipo::get).toList();
        pokemon.setDebilidades(CalculadorDebilidades.calcular(relaciones).stream().map(this::tipo).toList());

        int cadenaId = ConversorPokeApi.numeroDesdeUrl(especie.path("evolution_chain").path("url").asText());
        pokemon.setCadenaEvolutivaId(cadenaId);
        if (!evolucionRepository.existsByCadenaId(cadenaId)) {
            importarCadena(cadenaId);
        }

        pokemonRepository.save(pokemon);
    }

    private void importarCadena(int cadenaId) {
        List<EslabonCadena> eslabones = ConversorPokeApi.recorrerCadena(cliente.obtenerCadenaEvolutiva(cadenaId));
        List<Evolucion> evoluciones = eslabones.stream()
                .map(e -> new Evolucion(cadenaId, e.numero(), ConversorPokeApi.nombreLegible(e.clave()),
                        ConversorPokeApi.urlImagen(e.numero()), e.etapa(), e.orden()))
                .toList();
        evolucionRepository.saveAll(evoluciones);
    }

    /** Primera habilidad no oculta, con su nombre en español. */
    private String habilidadPrincipal(JsonNode datos) {
        return ordenadosPorSlot(datos.path("abilities")).stream()
                .filter(habilidad -> !habilidad.path("is_hidden").asBoolean())
                .findFirst()
                .map(habilidad -> habilidad.path("ability").path("name").asText())
                .map(clave -> habilidades.computeIfAbsent(clave, this::nombreHabilidad))
                .orElse("Desconocida");
    }

    private String nombreHabilidad(String clave) {
        return ConversorPokeApi.textoEnEspanol(cliente.obtenerHabilidad(clave).path("names"), "name")
                .orElse(ConversorPokeApi.nombreLegible(clave));
    }

    private static void asignarEstadisticas(Pokemon pokemon, JsonNode estadisticas) {
        Map<String, Integer> valores = new HashMap<>();
        for (JsonNode estadistica : estadisticas) {
            valores.put(estadistica.path("stat").path("name").asText(), estadistica.path("base_stat").asInt());
        }
        pokemon.setEstadisticas(
                valores.getOrDefault("hp", 0),
                valores.getOrDefault("attack", 0),
                valores.getOrDefault("defense", 0),
                valores.getOrDefault("special-attack", 0),
                valores.getOrDefault("special-defense", 0),
                valores.getOrDefault("speed", 0));
    }

    private static String imagen(JsonNode datos, int numero) {
        String url = datos.path("sprites").path("other").path("official-artwork").path("front_default").asText(null);
        return url != null ? url : ConversorPokeApi.urlImagen(numero);
    }

    private Tipo tipo(String clave) {
        Tipo tipo = tipos.get(clave);
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo desconocido: " + clave);
        }
        return tipo;
    }

    private static List<JsonNode> ordenadosPorSlot(JsonNode lista) {
        return StreamSupport.stream(lista.spliterator(), false)
                .sorted(Comparator.comparingInt(nodo -> nodo.path("slot").asInt()))
                .toList();
    }

    private static Set<String> nombres(JsonNode lista) {
        Set<String> nombres = new HashSet<>();
        lista.forEach(nodo -> nombres.add(nodo.path("name").asText()));
        return nombres;
    }
}
