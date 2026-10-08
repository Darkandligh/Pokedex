package co.edu.fet.pokedex.service;

import co.edu.fet.pokedex.dto.EstadisticasDto;
import co.edu.fet.pokedex.dto.EvolucionDto;
import co.edu.fet.pokedex.dto.PokemonDetalleDto;
import co.edu.fet.pokedex.dto.PokemonResumenDto;
import co.edu.fet.pokedex.dto.TipoDto;
import co.edu.fet.pokedex.entity.Evolucion;
import co.edu.fet.pokedex.entity.Pokemon;
import co.edu.fet.pokedex.entity.Tipo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Convierte las entidades en DTOs para no exponerlas directamente en la API.
 */
@Component
public class PokemonMapper {

    public TipoDto aTipoDto(Tipo tipo) {
        return new TipoDto(tipo.getClave(), tipo.getNombre());
    }

    public PokemonResumenDto aResumen(Pokemon pokemon) {
        return new PokemonResumenDto(pokemon.getNumero(), pokemon.getNombre(), pokemon.getImagenUrl(),
                aTiposDto(pokemon.getTipos()));
    }

    /**
     * @param cadena             eslabones de la cadena evolutiva del Pokémon
     * @param nombresDisponibles número → nombre de los eslabones que están en la Pokédex
     * @param anterior           número del Pokémon anterior (null si es el primero)
     * @param siguiente          número del Pokémon siguiente (null si es el último)
     */
    public PokemonDetalleDto aDetalle(Pokemon pokemon, List<Evolucion> cadena, Map<Integer, String> nombresDisponibles,
                                      Integer anterior, Integer siguiente) {
        List<EvolucionDto> cadenaDto = cadena.stream()
                .map(evolucion -> aEvolucionDto(evolucion, nombresDisponibles))
                .toList();
        return new PokemonDetalleDto(
                pokemon.getNumero(),
                pokemon.getNombre(),
                pokemon.getDescripcion(),
                pokemon.getImagenUrl(),
                pokemon.getAltura(),
                pokemon.getPeso(),
                pokemon.getGenero(),
                pokemon.getCategoria(),
                pokemon.getHabilidad(),
                aTiposDto(pokemon.getTipos()),
                aTiposDto(pokemon.getDebilidades()),
                new EstadisticasDto(pokemon.getPs(), pokemon.getAtaque(), pokemon.getDefensa(),
                        pokemon.getAtaqueEspecial(), pokemon.getDefensaEspecial(), pokemon.getVelocidad()),
                cadenaDto,
                anterior,
                siguiente);
    }

    private EvolucionDto aEvolucionDto(Evolucion evolucion, Map<Integer, String> nombresDisponibles) {
        String nombreEnPokedex = nombresDisponibles.get(evolucion.getNumero());
        boolean disponible = nombreEnPokedex != null;
        return new EvolucionDto(
                evolucion.getNumero(),
                disponible ? nombreEnPokedex : evolucion.getNombre(),
                evolucion.getImagenUrl(),
                evolucion.getEtapa(),
                disponible);
    }

    private List<TipoDto> aTiposDto(List<Tipo> tipos) {
        return tipos.stream().map(this::aTipoDto).toList();
    }
}
