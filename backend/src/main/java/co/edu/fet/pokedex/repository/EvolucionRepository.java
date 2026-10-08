package co.edu.fet.pokedex.repository;

import co.edu.fet.pokedex.entity.Evolucion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvolucionRepository extends JpaRepository<Evolucion, Long> {

    List<Evolucion> findByCadenaIdOrderByEtapaAscOrdenAsc(Integer cadenaId);

    boolean existsByCadenaId(Integer cadenaId);
}
