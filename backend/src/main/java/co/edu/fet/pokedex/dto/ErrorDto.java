package co.edu.fet.pokedex.dto;

/**
 * Respuesta de error de la API.
 */
public record ErrorDto(int estado, String mensaje) {
}
