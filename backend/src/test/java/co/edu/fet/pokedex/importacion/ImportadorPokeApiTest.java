package co.edu.fet.pokedex.importacion;

import co.edu.fet.pokedex.config.ImportacionProperties;
import co.edu.fet.pokedex.repository.EstadoImportacionRepository;
import co.edu.fet.pokedex.repository.EvolucionRepository;
import co.edu.fet.pokedex.repository.PokemonRepository;
import co.edu.fet.pokedex.repository.TipoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Cuándo se consulta PokéAPI al arrancar. Lo importante para la HU-13: un Pokémon que eliminó
 * un administrador no vuelve a importarse.
 */
@ExtendWith(MockitoExtension.class)
class ImportadorPokeApiTest {

    @Mock
    private PokeApiCliente cliente;
    @Mock
    private PokemonRepository pokemonRepository;
    @Mock
    private TipoRepository tipoRepository;
    @Mock
    private EvolucionRepository evolucionRepository;
    @Mock
    private EstadoImportacionRepository estadoRepository;

    private ImportadorPokeApi importador;

    @BeforeEach
    void preparar() {
        importador = new ImportadorPokeApi(cliente, new ImportacionProperties(true, "https://pokeapi.co/api/v2", 151, 0),
                pokemonRepository, tipoRepository, evolucionRepository, estadoRepository);
    }

    @Test
    void siLaImportacionYaTerminoNoConsultaPokeApiAunqueFaltenPokemon() {
        // 150 en la base: un administrador eliminó uno
        when(pokemonRepository.count()).thenReturn(150L);
        when(estadoRepository.importacionCompletada()).thenReturn(true);

        importador.run(null);

        verifyNoInteractions(cliente);
        verify(estadoRepository, never()).marcarCompletada();
    }

    @Test
    void unaBaseCompletaDeAntesQuedaMarcadaComoImportada() {
        when(pokemonRepository.count()).thenReturn(151L);
        when(estadoRepository.importacionCompletada()).thenReturn(false);

        importador.run(null);

        verifyNoInteractions(cliente);
        verify(estadoRepository).marcarCompletada();
    }
}
