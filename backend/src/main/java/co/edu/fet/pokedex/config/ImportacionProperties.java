package co.edu.fet.pokedex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración de la importación desde PokéAPI (prefijo pokedex.importacion).
 *
 * @param habilitada si es false, no se consulta PokéAPI al arrancar
 * @param urlBase    URL base de PokéAPI
 * @param cantidad   cuántos Pokémon importar desde el N.º 1 (151 = primera generación)
 * @param pausaMs    pausa entre peticiones para no saturar PokéAPI
 */
@ConfigurationProperties(prefix = "pokedex.importacion")
public record ImportacionProperties(
        @DefaultValue("true") boolean habilitada,
        @DefaultValue("https://pokeapi.co/api/v2") String urlBase,
        @DefaultValue("151") int cantidad,
        @DefaultValue("100") long pausaMs) {
}
