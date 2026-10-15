package co.edu.fet.pokedex.dto;

import java.util.Map;

/**
 * Respuesta de error cuando los datos enviados no son válidos (400).
 *
 * @param errores campo → mensaje (por ejemplo "estadisticas.ps" → "Los PS deben estar entre 1 y 255")
 */
public record ErrorValidacionDto(int estado, String mensaje, Map<String, String> errores) {
}
