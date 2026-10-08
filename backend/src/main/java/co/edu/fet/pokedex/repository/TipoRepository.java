package co.edu.fet.pokedex.repository;

import co.edu.fet.pokedex.entity.Tipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoRepository extends JpaRepository<Tipo, Long> {

    Optional<Tipo> findByClave(String clave);
}
