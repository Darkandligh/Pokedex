package co.edu.fet.pokedex.controller;

import co.edu.fet.pokedex.dto.PokemonDetalleDto;
import co.edu.fet.pokedex.service.PokemonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de la Pokédex.
 */
@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    /** Ficha completa por número (HU-01). */
    @GetMapping("/{numero}")
    public PokemonDetalleDto obtenerPorNumero(@PathVariable int numero) {
        return pokemonService.obtenerPorNumero(numero);
    }

    /** Ficha completa por nombre (HU-01). */
    @GetMapping("/nombre/{nombre}")
    public PokemonDetalleDto obtenerPorNombre(@PathVariable String nombre) {
        return pokemonService.obtenerPorNombre(nombre);
    }
}
