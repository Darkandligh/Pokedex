package co.edu.fet.pokedex.dto;

import java.util.List;

/**
 * Ficha completa de un Pokémon.
 *
 * @param altura    en metros
 * @param peso      en kilogramos
 * @param anterior  número del Pokémon anterior, o null si es el primero
 * @param siguiente número del Pokémon siguiente, o null si es el último
 */
public record PokemonDetalleDto(
        Integer numero,
        String nombre,
        String descripcion,
        String imagenUrl,
        Double altura,
        Double peso,
        String genero,
        String categoria,
        String habilidad,
        List<TipoDto> tipos,
        List<TipoDto> debilidades,
        EstadisticasDto estadisticas,
        List<EvolucionDto> cadenaEvolutiva,
        Integer anterior,
        Integer siguiente) {
}
