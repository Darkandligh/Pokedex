package co.edu.fet.pokedex.repository;

import co.edu.fet.pokedex.entity.Pokemon;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PokemonRepository extends JpaRepository<Pokemon, Integer> {

    /** Listado completo ordenado por número, cargando los tipos en la misma consulta. */
    @EntityGraph(attributePaths = "tipos")
    List<Pokemon> findAllByOrderByNumeroAsc();

    /** Búsqueda parcial por nombre sin distinguir mayúsculas, ordenada por número. */
    @EntityGraph(attributePaths = "tipos")
    List<Pokemon> findByNombreContainingIgnoreCaseOrderByNumeroAsc(String nombre);

    Optional<Pokemon> findByNombreIgnoreCase(String nombre);

    /** Número del Pokémon inmediatamente anterior, o null si es el primero. */
    @Query("select max(p.numero) from Pokemon p where p.numero < :numero")
    Integer buscarNumeroAnterior(@Param("numero") Integer numero);

    /** Número del Pokémon inmediatamente siguiente, o null si es el último. */
    @Query("select min(p.numero) from Pokemon p where p.numero > :numero")
    Integer buscarNumeroSiguiente(@Param("numero") Integer numero);
}
