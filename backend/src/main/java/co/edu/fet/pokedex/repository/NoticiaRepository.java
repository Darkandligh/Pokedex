package co.edu.fet.pokedex.repository;

import co.edu.fet.pokedex.entity.Noticia;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticiaRepository extends JpaRepository<Noticia, Long> {

    /** Las más recientes primero; con la misma fecha, la última registrada primero. */
    List<Noticia> findAllByOrderByFechaPublicacionDescIdDesc(Pageable pagina);
}
