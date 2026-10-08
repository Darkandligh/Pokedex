package co.edu.fet.pokedex.repository;

import co.edu.fet.pokedex.entity.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PokemonRepository extends JpaRepository<Pokemon, Integer> {

    Optional<Pokemon> findByNombreIgnoreCase(String nombre);
}
