package co.edu.fet.pokedex.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Las 6 estadísticas base de un Pokémon. Al registrar o editar (HU-13) cada una va de 1 a 255.
 */
public record EstadisticasDto(
        @NotNull(message = "Los PS son obligatorios")
        @Min(value = 1, message = "Los PS deben estar entre 1 y 255")
        @Max(value = 255, message = "Los PS deben estar entre 1 y 255")
        Integer ps,

        @NotNull(message = "El ataque es obligatorio")
        @Min(value = 1, message = "El ataque debe estar entre 1 y 255")
        @Max(value = 255, message = "El ataque debe estar entre 1 y 255")
        Integer ataque,

        @NotNull(message = "La defensa es obligatoria")
        @Min(value = 1, message = "La defensa debe estar entre 1 y 255")
        @Max(value = 255, message = "La defensa debe estar entre 1 y 255")
        Integer defensa,

        @NotNull(message = "El ataque especial es obligatorio")
        @Min(value = 1, message = "El ataque especial debe estar entre 1 y 255")
        @Max(value = 255, message = "El ataque especial debe estar entre 1 y 255")
        Integer ataqueEspecial,

        @NotNull(message = "La defensa especial es obligatoria")
        @Min(value = 1, message = "La defensa especial debe estar entre 1 y 255")
        @Max(value = 255, message = "La defensa especial debe estar entre 1 y 255")
        Integer defensaEspecial,

        @NotNull(message = "La velocidad es obligatoria")
        @Min(value = 1, message = "La velocidad debe estar entre 1 y 255")
        @Max(value = 255, message = "La velocidad debe estar entre 1 y 255")
        Integer velocidad) {
}
