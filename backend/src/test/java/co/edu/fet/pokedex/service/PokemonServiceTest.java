package co.edu.fet.pokedex.service;

import co.edu.fet.pokedex.dto.PokemonDetalleDto;
import co.edu.fet.pokedex.entity.Evolucion;
import co.edu.fet.pokedex.entity.Pokemon;
import co.edu.fet.pokedex.entity.Tipo;
import co.edu.fet.pokedex.exception.PokemonNoEncontradoException;
import co.edu.fet.pokedex.repository.EvolucionRepository;
import co.edu.fet.pokedex.repository.PokemonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio, con los repositorios simulados.
 */
@ExtendWith(MockitoExtension.class)
class PokemonServiceTest {

    @Mock
    private PokemonRepository pokemonRepository;
    @Mock
    private EvolucionRepository evolucionRepository;

    private PokemonService servicio;

    private final Tipo fuego = new Tipo("fire", "Fuego");
    private final Tipo agua = new Tipo("water", "Agua");

    @BeforeEach
    void preparar() {
        servicio = new PokemonService(pokemonRepository, evolucionRepository, new PokemonMapper());
    }

    private Pokemon charmander() {
        Pokemon pokemon = new Pokemon(4, "Charmander");
        pokemon.setDescripcion("Prefiere las cosas calientes.");
        pokemon.setImagenUrl("https://img/4.png");
        pokemon.setAltura(0.6);
        pokemon.setPeso(8.5);
        pokemon.setGenero("Macho y hembra");
        pokemon.setCategoria("Lagartija");
        pokemon.setHabilidad("Mar Llamas");
        pokemon.setEstadisticas(39, 52, 43, 60, 50, 65);
        pokemon.setTipos(List.of(fuego));
        pokemon.setDebilidades(List.of(agua));
        pokemon.setCadenaEvolutivaId(2);
        return pokemon;
    }

    @Test
    void listarDevuelveElResumenEnElOrdenDelRepositorio() {
        Pokemon squirtle = new Pokemon(7, "Squirtle");
        squirtle.setTipos(List.of(agua));
        when(pokemonRepository.findAllByOrderByNumeroAsc()).thenReturn(List.of(charmander(), squirtle));

        assertThat(servicio.listar(null))
                .extracting("numero", "nombre")
                .containsExactly(org.assertj.core.groups.Tuple.tuple(4, "Charmander"),
                        org.assertj.core.groups.Tuple.tuple(7, "Squirtle"));
        assertThat(servicio.listar("  ").get(1).tipos()).extracting("nombre").containsExactly("Agua");
    }

    @Test
    void buscarPorNumeroAceptaCerosALaIzquierda() {
        when(pokemonRepository.findById(4)).thenReturn(Optional.of(charmander()));

        for (String busqueda : List.of("4", "004", "0004")) {
            assertThat(servicio.listar(busqueda)).extracting("nombre").containsExactly("Charmander");
        }
    }

    @Test
    void buscarPorNombreUsaLaBusquedaParcial() {
        when(pokemonRepository.findByNombreContainingIgnoreCaseOrderByNumeroAsc("char"))
                .thenReturn(List.of(charmander()));

        assertThat(servicio.listar(" char ")).extracting("nombre").containsExactly("Charmander");
    }

    @Test
    void buscarSinCoincidenciasDevuelveListaVacia() {
        when(pokemonRepository.findById(9999)).thenReturn(Optional.empty());
        when(pokemonRepository.findByNombreContainingIgnoreCaseOrderByNumeroAsc("zzz")).thenReturn(List.of());

        assertThat(servicio.listar("9999")).isEmpty();
        assertThat(servicio.listar("zzz")).isEmpty();
    }

    @Test
    void obtenerPorNumeroArmaLaFichaCompleta() {
        Pokemon charmander = charmander();
        when(pokemonRepository.findById(4)).thenReturn(Optional.of(charmander));
        when(evolucionRepository.findByCadenaIdOrderByEtapaAscOrdenAsc(2)).thenReturn(List.of(
                new Evolucion(2, 4, "Charmander", "https://img/4.png", 0, 0),
                new Evolucion(2, 5, "Charmeleon", "https://img/5.png", 1, 1)));
        when(pokemonRepository.findAllById(anyIterable())).thenReturn(List.of(charmander));

        PokemonDetalleDto ficha = servicio.obtenerPorNumero(4);

        assertThat(ficha.numero()).isEqualTo(4);
        assertThat(ficha.nombre()).isEqualTo("Charmander");
        assertThat(ficha.categoria()).isEqualTo("Lagartija");
        assertThat(ficha.tipos()).extracting("nombre").containsExactly("Fuego");
        assertThat(ficha.debilidades()).extracting("nombre").containsExactly("Agua");
        assertThat(ficha.estadisticas().ps()).isEqualTo(39);
        assertThat(ficha.estadisticas().velocidad()).isEqualTo(65);
        // Charmeleon no está en la Pokédex simulada: aparece en la cadena pero sin enlace
        assertThat(ficha.cadenaEvolutiva()).extracting("numero", "disponible")
                .containsExactly(org.assertj.core.groups.Tuple.tuple(4, true),
                        org.assertj.core.groups.Tuple.tuple(5, false));
    }

    @Test
    void obtenerPorNumeroInexistenteLanzaNoEncontrado() {
        when(pokemonRepository.findById(9999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtenerPorNumero(9999))
                .isInstanceOf(PokemonNoEncontradoException.class)
                .hasMessage("Pokémon no encontrado");
    }

    @Test
    void obtenerPorNombreIgnoraEspaciosYMayusculas() {
        when(pokemonRepository.findByNombreIgnoreCase("charmander")).thenReturn(Optional.of(charmander()));

        assertThat(servicio.obtenerPorNombre("  charmander ").nombre()).isEqualTo("Charmander");
    }

    @Test
    void obtenerPorNombreInexistenteLanzaNoEncontrado() {
        when(pokemonRepository.findByNombreIgnoreCase("missingno")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtenerPorNombre("missingno"))
                .isInstanceOf(PokemonNoEncontradoException.class);
    }
}
