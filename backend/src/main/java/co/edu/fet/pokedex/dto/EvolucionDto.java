package co.edu.fet.pokedex.dto;

/**
 * Eslabón de la cadena evolutiva.
 *
 * @param etapa      0 = forma base, 1 = primera evolución, 2 = segunda evolución
 * @param disponible true si el Pokémon está en la Pokédex y se puede abrir su ficha
 */
public record EvolucionDto(Integer numero, String nombre, String imagenUrl, Integer etapa, boolean disponible) {
}
