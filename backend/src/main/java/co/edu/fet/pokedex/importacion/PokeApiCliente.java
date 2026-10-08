package co.edu.fet.pokedex.importacion;

import co.edu.fet.pokedex.config.ImportacionProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Cliente HTTP mínimo para PokéAPI. Hace una pausa antes de cada petición
 * para respetar el servicio público.
 */
@Component
public class PokeApiCliente {

    private final RestClient restClient;
    private final long pausaMs;

    public PokeApiCliente(ImportacionProperties propiedades) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(5));
        fabrica.setReadTimeout(Duration.ofSeconds(20));
        this.restClient = RestClient.builder()
                .baseUrl(propiedades.urlBase())
                .requestFactory(fabrica)
                .defaultHeader("User-Agent", "pokedex-academico-fet")
                .build();
        this.pausaMs = propiedades.pausaMs();
    }

    public JsonNode obtenerPokemon(int numero) {
        return consultar("/pokemon/{id}", numero);
    }

    public JsonNode obtenerEspecie(int numero) {
        return consultar("/pokemon-species/{id}", numero);
    }

    public JsonNode obtenerCadenaEvolutiva(int cadenaId) {
        return consultar("/evolution-chain/{id}", cadenaId);
    }

    public JsonNode obtenerTipo(String clave) {
        return consultar("/type/{nombre}", clave);
    }

    public JsonNode obtenerHabilidad(String clave) {
        return consultar("/ability/{nombre}", clave);
    }

    private JsonNode consultar(String ruta, Object parametro) {
        pausar();
        return restClient.get().uri(ruta, parametro).retrieve().body(JsonNode.class);
    }

    private void pausar() {
        if (pausaMs <= 0) {
            return;
        }
        try {
            Thread.sleep(pausaMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
