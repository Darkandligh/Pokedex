package co.edu.fet.pokedex.importacion;

import co.edu.fet.pokedex.importacion.ConversorPokeApi.EslabonCadena;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConversorPokeApiTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode json(String texto) throws Exception {
        return objectMapper.readTree(texto.replace('\'', '"'));
    }

    @Test
    void tomaLaDescripcionYLaCategoriaEnEspanol() throws Exception {
        JsonNode especie = json("""
                {'flavor_text_entries': [
                   {'flavor_text': 'A strange seed', 'language': {'name': 'en'}},
                   {'flavor_text': 'Una rara semilla\\nle fue plantada\\fal nacer.', 'language': {'name': 'es'}}],
                 'genera': [
                   {'genus': 'Seed Pokémon', 'language': {'name': 'en'}},
                   {'genus': 'Pokémon Semilla', 'language': {'name': 'es'}}]}
                """);

        assertThat(ConversorPokeApi.descripcion(especie)).isEqualTo("Una rara semilla le fue plantada al nacer.");
        assertThat(ConversorPokeApi.categoria(especie)).isEqualTo("Semilla");
    }

    @Test
    void sinTextoEnEspanolUsaUnValorPorDefecto() throws Exception {
        JsonNode especie = json("{'flavor_text_entries': [], 'genera': []}");

        assertThat(ConversorPokeApi.descripcion(especie)).isEqualTo("Sin descripción disponible.");
        assertThat(ConversorPokeApi.categoria(especie)).isEqualTo("Desconocida");
    }

    @Test
    void interpretaLaTasaDeGenero() {
        assertThat(ConversorPokeApi.genero(-1)).isEqualTo("Desconocido");
        assertThat(ConversorPokeApi.genero(0)).isEqualTo("Macho");
        assertThat(ConversorPokeApi.genero(8)).isEqualTo("Hembra");
        assertThat(ConversorPokeApi.genero(1)).isEqualTo("Macho y hembra");
    }

    @Test
    void extraeElNumeroDeUnaUrl() {
        assertThat(ConversorPokeApi.numeroDesdeUrl("https://pokeapi.co/api/v2/pokemon-species/25/")).isEqualTo(25);
        assertThat(ConversorPokeApi.numeroDesdeUrl("https://pokeapi.co/api/v2/evolution-chain/10")).isEqualTo(10);
    }

    @Test
    void recorreUnaCadenaConRamificaciones() throws Exception {
        JsonNode cadena = json("""
                {'chain': {'species': {'name': 'eevee', 'url': '.../pokemon-species/133/'},
                  'evolves_to': [
                    {'species': {'name': 'vaporeon', 'url': '.../pokemon-species/134/'}, 'evolves_to': []},
                    {'species': {'name': 'jolteon', 'url': '.../pokemon-species/135/'}, 'evolves_to': []}]}}
                """);

        assertThat(ConversorPokeApi.recorrerCadena(cadena)).containsExactly(
                new EslabonCadena(133, "eevee", 0, 0),
                new EslabonCadena(134, "vaporeon", 1, 1),
                new EslabonCadena(135, "jolteon", 1, 2));
    }

    @Test
    void convierteIdentificadoresEnNombresLegibles() {
        assertThat(ConversorPokeApi.nombreLegible("mr-mime")).isEqualTo("Mr Mime");
        assertThat(ConversorPokeApi.nombreLegible("pichu")).isEqualTo("Pichu");
    }
}
