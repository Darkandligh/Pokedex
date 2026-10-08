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

    /** Listado de la Pokédex ordenado por número. */
    public List<PokemonResumenDto> listar() {
        return pokemonRepository.findAllByOrderByNumeroAsc().stream()
                .map(mapper::aResumen)
                .toList();
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
        return mapper.aDetalle(pokemon, cadena, nombresEnPokedex(cadena));
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
