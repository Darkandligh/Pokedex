package co.edu.fet.pokedex.dto;

import java.time.LocalDate;

/**
 * Noticia para la portada (HU-06).
 *
 * @param fecha fecha de publicación, en formato ISO (2026-10-15)
 */
public record NoticiaDto(Long id, String titulo, String resumen, String imagenUrl, LocalDate fecha) {
}
