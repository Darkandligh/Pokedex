package co.edu.fet.pokedex.importacion;

import co.edu.fet.pokedex.entity.Noticia;
import co.edu.fet.pokedex.repository.NoticiaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Carga las noticias de ejemplo de noticias-ejemplo.json (HU-06) si la base no tiene ninguna.
 * Corre en todos los perfiles; no necesita internet.
 */
@Component
public class CargadorNoticias implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargadorNoticias.class);
    private static final String ARCHIVO = "noticias-ejemplo.json";

    record NoticiaEjemplo(String titulo, String resumen, Integer pokemon, LocalDate fecha) {
    }

    private final ObjectMapper objectMapper;
    private final NoticiaRepository noticiaRepository;

    public CargadorNoticias(ObjectMapper objectMapper, NoticiaRepository noticiaRepository) {
        this.objectMapper = objectMapper;
        this.noticiaRepository = noticiaRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (noticiaRepository.count() > 0) {
            return;
        }
        List<NoticiaEjemplo> ejemplos;
        try (InputStream entrada = new ClassPathResource(ARCHIVO).getInputStream()) {
            ejemplos = List.of(objectMapper.readValue(entrada, NoticiaEjemplo[].class));
        }
        // La imagen de cada noticia es la imagen oficial de un Pokémon relacionado
        noticiaRepository.saveAll(ejemplos.stream()
                .map(e -> new Noticia(e.titulo(), e.resumen(),
                        e.pokemon() == null ? null : ConversorPokeApi.urlImagen(e.pokemon()), e.fecha()))
                .toList());
        log.info("Noticias de ejemplo cargadas: {}.", ejemplos.size());
    }
}
