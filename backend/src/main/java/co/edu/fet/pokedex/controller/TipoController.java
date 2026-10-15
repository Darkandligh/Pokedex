package co.edu.fet.pokedex.controller;

import co.edu.fet.pokedex.dto.TipoDto;
import co.edu.fet.pokedex.importacion.CalculadorDebilidades;
import co.edu.fet.pokedex.repository.TipoRepository;
import co.edu.fet.pokedex.service.PokemonMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

/**
 * Tipos de Pokémon que hay en la base, en el orden oficial (Normal, Lucha, Volador...).
 * Los usan el filtro por tipo del listado (HU-04) y el formulario de administración (HU-13).
 */
@RestController
@RequestMapping("/api/tipos")
public class TipoController {

    private final TipoRepository tipoRepository;
    private final PokemonMapper mapper;

    public TipoController(TipoRepository tipoRepository, PokemonMapper mapper) {
        this.tipoRepository = tipoRepository;
        this.mapper = mapper;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<TipoDto> listar() {
        return tipoRepository.findAll().stream()
                .sorted(Comparator.comparingInt(tipo -> posicionOficial(tipo.getClave())))
                .map(mapper::aTipoDto)
                .toList();
    }

    private static int posicionOficial(String clave) {
        int posicion = CalculadorDebilidades.TIPOS_OFICIALES.indexOf(clave);
        return posicion < 0 ? Integer.MAX_VALUE : posicion;
    }
}
