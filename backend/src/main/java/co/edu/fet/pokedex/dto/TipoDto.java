package co.edu.fet.pokedex.dto;

/**
 * @param clave  identificador del tipo (por ejemplo "fire"), útil para asignar colores
 * @param nombre nombre en español (por ejemplo "Fuego")
 */
public record TipoDto(String clave, String nombre) {
}
