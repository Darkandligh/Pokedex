package co.edu.fet.pokedex.importacion;

import co.edu.fet.pokedex.entity.Evolucion;
import co.edu.fet.pokedex.entity.Pokemon;
import co.edu.fet.pokedex.entity.Tipo;
import co.edu.fet.pokedex.repository.EvolucionRepository;
import co.edu.fet.pokedex.repository.PokemonRepository;
import co.edu.fet.pokedex.repository.TipoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Perfil "datosfijos": carga los Pokémon N.º 0001 a 0009 desde datos-fijos.json,
 * sin conexión a internet. Lo usan las pruebas automáticas.
 */
@Component
@Profile("datosfijos")
public class CargadorDatosFijos implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargadorDatosFijos.class);
    private static final String ARCHIVO = "datos-fijos.json";

    record TipoFijo(String clave, String nombre) {
    }

    record PokemonFijo(int numero, String nombre, String descripcion, double altura, double peso,
                       String genero, String categoria, String habilidad, List<Integer> estadisticas,
                       List<String> tipos, List<String> debilidades, int cadenaEvolutivaId) {
    }

    record EvolucionFija(int cadenaId, int numero, String nombre, int etapa, int orden) {
    }

    record DatosFijos(List<TipoFijo> tipos, List<PokemonFijo> pokemon, List<EvolucionFija> evoluciones) {
    }

    private final ObjectMapper objectMapper;
    private final PokemonRepository pokemonRepository;
    private final TipoRepository tipoRepository;
    private final EvolucionRepository evolucionRepository;

    public CargadorDatosFijos(ObjectMapper objectMapper, PokemonRepository pokemonRepository,
                              TipoRepository tipoRepository, EvolucionRepository evolucionRepository) {
        this.objectMapper = objectMapper;
        this.pokemonRepository = pokemonRepository;
        this.tipoRepository = tipoRepository;
        this.evolucionRepository = evolucionRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (pokemonRepository.count() > 0) {
            return;
        }
        DatosFijos datos;
        try (InputStream entrada = new ClassPathResource(ARCHIVO).getInputStream()) {
            datos = objectMapper.readValue(entrada, DatosFijos.class);
        }

        Map<String, Tipo> tipos = tipoRepository.saveAll(
                        datos.tipos().stream().map(t -> new Tipo(t.clave(), t.nombre())).toList())
                .stream()
                .collect(Collectors.toMap(Tipo::getClave, Function.identity()));

        evolucionRepository.saveAll(datos.evoluciones().stream()
                .map(e -> new Evolucion(e.cadenaId(), e.numero(), e.nombre(),
                        ConversorPokeApi.urlImagen(e.numero()), e.etapa(), e.orden()))
                .toList());

        for (PokemonFijo fijo : datos.pokemon()) {
            Pokemon pokemon = new Pokemon(fijo.numero(), fijo.nombre());
            pokemon.setDescripcion(fijo.descripcion());
            pokemon.setImagenUrl(ConversorPokeApi.urlImagen(fijo.numero()));
            pokemon.setAltura(fijo.altura());
            pokemon.setPeso(fijo.peso());
            pokemon.setGenero(fijo.genero());
            pokemon.setCategoria(fijo.categoria());
            pokemon.setHabilidad(fijo.habilidad());
            List<Integer> e = fijo.estadisticas();
            pokemon.setEstadisticas(e.get(0), e.get(1), e.get(2), e.get(3), e.get(4), e.get(5));
            pokemon.setTipos(fijo.tipos().stream().map(tipos::get).toList());
            pokemon.setDebilidades(fijo.debilidades().stream().map(tipos::get).toList());
            pokemon.setCadenaEvolutivaId(fijo.cadenaEvolutivaId());
            pokemonRepository.save(pokemon);
        }
        log.info("Datos fijos cargados: {} Pokémon.", datos.pokemon().size());
    }
}
