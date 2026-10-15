package co.edu.fet.pokedex.service;

import co.edu.fet.pokedex.dto.PokemonDetalleDto;
import co.edu.fet.pokedex.dto.PokemonResumenDto;
import co.edu.fet.pokedex.entity.Evolucion;
import co.edu.fet.pokedex.entity.Pokemon;
import co.edu.fet.pokedex.exception.PokemonNoEncontradoException;
import co.edu.fet.pokedex.repository.EvolucionRepository;
import co.edu.fet.pokedex.repository.PokemonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Consultas de la Pokédex.
 */
@Service
@Transactional(readOnly = true)
public class PokemonService {

    private final PokemonRepository pokemonRepository;
    private final EvolucionRepository evolucionRepository;
    private final PokemonMapper mapper;

    public PokemonService(PokemonRepository pokemonRepository, EvolucionRepository evolucionRepository,
                          PokemonMapper mapper) {
        this.pokemonRepository = pokemonRepository;
        this.evolucionRepository = evolucionRepository;
        this.mapper = mapper;
    }

    /**
     * Listado de la Pokédex ordenado por número, filtrado opcionalmente.
     *
     * @param busqueda vacío: todos; solo dígitos ("1", "001", "0001"): ese número exacto;
     *                 otro texto: nombres que lo contengan, sin distinguir mayúsculas
     */
    public List<PokemonResumenDto> listar(String busqueda) {
        return listar(busqueda, null);
    }

    /**
     * Listado filtrado por búsqueda (HU-03) y por tipo (HU-04); los dos filtros se combinan.
     *
     * @param tipo clave del tipo ("fire"), sin distinguir mayúsculas; cuenta tanto el tipo
     *             principal como el secundario. Vacío: todos los tipos
     */
    public List<PokemonResumenDto> listar(String busqueda, String tipo) {
        String texto = busqueda == null ? "" : busqueda.trim();
        String claveTipo = tipo == null ? "" : tipo.trim();
        List<Pokemon> resultado;
        if (texto.isEmpty()) {
            resultado = pokemonRepository.findAllByOrderByNumeroAsc();
        } else if (texto.matches("\\d{1,9}")) {
            resultado = pokemonRepository.findById(Integer.parseInt(texto)).map(List::of).orElse(List.of());
        } else {
            resultado = pokemonRepository.findByNombreContainingIgnoreCaseOrderByNumeroAsc(texto);
        }
        return resultado.stream()
                .filter(pokemon -> claveTipo.isEmpty() || tieneTipo(pokemon, claveTipo))
                .map(mapper::aResumen)
                .toList();
    }

    private static boolean tieneTipo(Pokemon pokemon, String claveTipo) {
        return pokemon.getTipos().stream().anyMatch(tipo -> tipo.getClave().equalsIgnoreCase(claveTipo));
    }

    /** Ficha completa por número de la Pokédex. */
    public PokemonDetalleDto obtenerPorNumero(int numero) {
        Pokemon pokemon = pokemonRepository.findById(numero)
                .orElseThrow(PokemonNoEncontradoException::new);
        return construirFicha(pokemon);
    }

    /** Ficha completa por nombre, sin distinguir mayúsculas. */
    public PokemonDetalleDto obtenerPorNombre(String nombre) {
        Pokemon pokemon = pokemonRepository.findByNombreIgnoreCase(nombre.trim())
                .orElseThrow(PokemonNoEncontradoException::new);
        return construirFicha(pokemon);
    }

    private PokemonDetalleDto construirFicha(Pokemon pokemon) {
        List<Evolucion> cadena = pokemon.getCadenaEvolutivaId() == null
                ? List.of()
                : evolucionRepository.findByCadenaIdOrderByEtapaAscOrdenAsc(pokemon.getCadenaEvolutivaId());
        return mapper.aDetalle(pokemon, cadena, nombresEnPokedex(cadena),
                pokemonRepository.buscarNumeroAnterior(pokemon.getNumero()),
                pokemonRepository.buscarNumeroSiguiente(pokemon.getNumero()));
    }

    /** Número → nombre de los eslabones de la cadena que sí están en la Pokédex. */
    private Map<Integer, String> nombresEnPokedex(List<Evolucion> cadena) {
        if (cadena.isEmpty()) {
            return Map.of();
        }
        List<Integer> numeros = cadena.stream().map(Evolucion::getNumero).toList();
        return pokemonRepository.findAllById(numeros).stream()
                .collect(Collectors.toMap(Pokemon::getNumero, Pokemon::getNombre));
    }
}
