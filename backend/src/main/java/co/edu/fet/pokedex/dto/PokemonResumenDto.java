package co.edu.fet.pokedex.dto;

import java.util.List;

/**
 * Datos de un Pokémon para el listado de la Pokédex.
 */
public record PokemonResumenDto(Integer numero, String nombre, String imagenUrl, List<TipoDto> tipos) {
}
