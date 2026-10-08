package co.edu.fet.pokedex.repository;

import co.edu.fet.pokedex.entity.Pokemon;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PokemonRepository extends JpaRepository<Pokemon, Integer> {

    /** Listado completo ordenado por número, cargando los tipos en la misma consulta. */
    @EntityGraph(attributePaths = "tipos")
    List<Pokemon> findAllByOrderByNumeroAsc();

    Optional<Pokemon> findByNombreIgnoreCase(String nombre);
}
