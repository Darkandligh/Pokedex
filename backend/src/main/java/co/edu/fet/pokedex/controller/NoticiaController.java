package co.edu.fet.pokedex.controller;

import co.edu.fet.pokedex.dto.NoticiaDto;
import co.edu.fet.pokedex.repository.NoticiaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Noticias recientes para la portada (HU-06).
 */
@RestController
@RequestMapping("/api/noticias")
public class NoticiaController {

    static final int LIMITE_MAXIMO = 20;

    private final NoticiaRepository noticiaRepository;

    public NoticiaController(NoticiaRepository noticiaRepository) {
        this.noticiaRepository = noticiaRepository;
    }

    /**
     * Las noticias más recientes primero. Sin noticias devuelve una lista vacía.
     *
     * @param limite cuántas devolver (de 1 a 20; 6 por defecto)
     */
    @GetMapping
    @Transactional(readOnly = true)
    public List<NoticiaDto> recientes(@RequestParam(defaultValue = "6") int limite) {
        int cantidad = Math.clamp(limite, 1, LIMITE_MAXIMO);
        return noticiaRepository.findAllByOrderByFechaPublicacionDescIdDesc(PageRequest.of(0, cantidad)).stream()
                .map(n -> new NoticiaDto(n.getId(), n.getTitulo(), n.getResumen(), n.getImagenUrl(), n.getFechaPublicacion()))
                .toList();
    }
}
