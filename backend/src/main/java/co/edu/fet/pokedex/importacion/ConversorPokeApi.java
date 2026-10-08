package co.edu.fet.pokedex.importacion;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Funciones para extraer los datos que necesita la Pokédex de las respuestas JSON de PokéAPI.
 * No hace peticiones: solo transforma, por eso se puede probar sin internet.
 */
public final class ConversorPokeApi {

    public static final String IDIOMA = "es";

    private static final Pattern NUMERO_AL_FINAL = Pattern.compile("/(\\d+)/?$");
    private static final String URL_IMAGEN =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%d.png";

    /** Eslabón de una cadena evolutiva leído de /evolution-chain. */
    public record EslabonCadena(int numero, String clave, int etapa, int orden) {
    }

    private ConversorPokeApi() {
    }

    /** Busca en una lista de traducciones (names, genera...) el valor del campo para el idioma español. */
    public static Optional<String> textoEnEspanol(JsonNode traducciones, String campo) {
        if (traducciones == null) {
            return Optional.empty();
        }
        for (JsonNode traduccion : traducciones) {
            if (IDIOMA.equals(traduccion.path("language").path("name").asText())) {
                return Optional.of(limpiarTexto(traduccion.path(campo).asText()));
            }
        }
        return Optional.empty();
    }

    /** Descripción de la Pokédex en español (primer flavor_text en "es"). */
    public static String descripcion(JsonNode especie) {
        return textoEnEspanol(especie.path("flavor_text_entries"), "flavor_text")
                .orElse("Sin descripción disponible.");
    }

    /** Categoría en español sin el prefijo "Pokémon" (por ejemplo, "Pokémon Semilla" → "Semilla"). */
    public static String categoria(JsonNode especie) {
        return textoEnEspanol(especie.path("genera"), "genus")
                .map(texto -> texto.replaceFirst("(?i)^Pokémon\\s+", ""))
                .orElse("Desconocida");
    }

    /**
     * Género a partir de gender_rate: -1 sin género; 0 solo machos; 8 solo hembras;
     * cualquier otro valor indica que hay de ambos.
     */
    public static String genero(int tasaGenero) {
        return switch (tasaGenero) {
            case -1 -> "Desconocido";
            case 0 -> "Macho";
            case 8 -> "Hembra";
            default -> "Macho y hembra";
        };
    }

    /** Extrae el id numérico del final de una URL de PokéAPI (".../pokemon-species/25/" → 25). */
    public static int numeroDesdeUrl(String url) {
        Matcher matcher = NUMERO_AL_FINAL.matcher(url);
        if (!matcher.find()) {
            throw new IllegalArgumentException("URL sin número: " + url);
        }
        return Integer.parseInt(matcher.group(1));
    }

    public static String urlImagen(int numero) {
        return String.format(URL_IMAGEN, numero);
    }

    /** Convierte un identificador de PokéAPI en un nombre legible ("mr-mime" → "Mr Mime"). */
    public static String nombreLegible(String clave) {
        return Arrays.stream(clave.split("-"))
                .filter(parte -> !parte.isBlank())
                .map(parte -> Character.toUpperCase(parte.charAt(0)) + parte.substring(1))
                .collect(Collectors.joining(" "));
    }

    /** Recorre la cadena evolutiva en profundidad y devuelve sus eslabones en orden. */
    public static List<EslabonCadena> recorrerCadena(JsonNode cadena) {
        List<EslabonCadena> eslabones = new ArrayList<>();
        agregarEslabon(cadena.path("chain"), 0, eslabones);
        return eslabones;
    }

    private static void agregarEslabon(JsonNode nodo, int etapa, List<EslabonCadena> eslabones) {
        JsonNode especie = nodo.path("species");
        eslabones.add(new EslabonCadena(
                numeroDesdeUrl(especie.path("url").asText()),
                especie.path("name").asText(),
                etapa,
                eslabones.size()));
        for (JsonNode siguiente : nodo.path("evolves_to")) {
            agregarEslabon(siguiente, etapa + 1, eslabones);
        }
    }

    /** Quita saltos de línea, caracteres de control y guiones suaves que trae PokéAPI. */
    static String limpiarTexto(String texto) {
        return texto.replaceAll("\\u00AD\\s*", "")
                .replaceAll("[\\n\\r\\f]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
