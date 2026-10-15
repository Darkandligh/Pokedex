package co.edu.fet.pokedex.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Datos para registrar o editar un Pokémon (HU-13).
 *
 * @param numero      obligatorio al registrar; al editar no se puede cambiar
 * @param imagenUrl   opcional: si va vacía se usa la imagen oficial del número
 * @param altura      en metros
 * @param peso        en kilogramos
 * @param tipos       claves de uno o dos tipos ("fire", "flying")
 */
public record PokemonFormularioDto(
        @Min(value = 1, message = "El número debe estar entre 1 y 9999")
        @Max(value = 9999, message = "El número debe estar entre 1 y 9999")
        Integer numero,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre puede tener máximo 50 caracteres")
        String nombre,

        @Size(max = 1000, message = "La descripción puede tener máximo 1000 caracteres")
        String descripcion,

        @Size(max = 300, message = "La URL de la imagen puede tener máximo 300 caracteres")
        @Pattern(regexp = "^$|^https?://\\S+$", message = "La imagen debe ser una URL que empiece por http:// o https://")
        String imagenUrl,

        @NotNull(message = "La altura es obligatoria")
        @DecimalMin(value = "0.1", message = "La altura debe estar entre 0,1 y 100 m")
        @DecimalMax(value = "100", message = "La altura debe estar entre 0,1 y 100 m")
        Double altura,

        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "0.1", message = "El peso debe estar entre 0,1 y 1000 kg")
        @DecimalMax(value = "1000", message = "El peso debe estar entre 0,1 y 1000 kg")
        Double peso,

        @NotBlank(message = "El género es obligatorio")
        @Pattern(regexp = "Macho y hembra|Macho|Hembra|Desconocido",
                message = "El género debe ser Macho y hembra, Macho, Hembra o Desconocido")
        String genero,

        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 80, message = "La categoría puede tener máximo 80 caracteres")
        String categoria,

        @NotBlank(message = "La habilidad es obligatoria")
        @Size(max = 80, message = "La habilidad puede tener máximo 80 caracteres")
        String habilidad,

        @NotNull(message = "Elige uno o dos tipos")
        @Size(min = 1, max = 2, message = "Elige uno o dos tipos")
        List<@NotBlank(message = "El tipo no puede estar vacío") String> tipos,

        @NotNull(message = "Las estadísticas son obligatorias")
        @Valid
        EstadisticasDto estadisticas) {
}
