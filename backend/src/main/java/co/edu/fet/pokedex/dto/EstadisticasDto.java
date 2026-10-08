package co.edu.fet.pokedex.dto;

/**
 * Las 6 estadísticas base de un Pokémon.
 */
public record EstadisticasDto(Integer ps, Integer ataque, Integer defensa,
                              Integer ataqueEspecial, Integer defensaEspecial, Integer velocidad) {
}
