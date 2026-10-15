package co.edu.fet.pokedex.repository;

import co.edu.fet.pokedex.entity.EstadoImportacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadoImportacionRepository extends JpaRepository<EstadoImportacion, Integer> {

    default boolean importacionCompletada() {
        return findById(EstadoImportacion.ID).map(EstadoImportacion::isCompletada).orElse(false);
    }

    default void marcarCompletada() {
        save(EstadoImportacion.completada());
    }
}
