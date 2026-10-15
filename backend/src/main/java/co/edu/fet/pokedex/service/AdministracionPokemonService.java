package co.edu.fet.pokedex.service;

import co.edu.fet.pokedex.dto.EstadisticasDto;
import co.edu.fet.pokedex.dto.PokemonDetalleDto;
import co.edu.fet.pokedex.dto.PokemonFormularioDto;
import co.edu.fet.pokedex.entity.Pokemon;
import co.edu.fet.pokedex.entity.Tipo;
import co.edu.fet.pokedex.exception.DatosInvalidosException;
import co.edu.fet.pokedex.exception.PokemonDuplicadoException;
import co.edu.fet.pokedex.exception.PokemonNoEncontradoException;
import co.edu.fet.pokedex.importacion.ConversorPokeApi;
import co.edu.fet.pokedex.importacion.TablaTipos;
import co.edu.fet.pokedex.repository.PokemonRepository;
import co.edu.fet.pokedex.repository.TipoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registrar, editar y eliminar Pokémon (HU-13). Las validaciones de cada campo están en
 * {@link PokemonFormularioDto}; aquí van las que dependen de la base de datos.
 */
@Service
@Transactional
public class AdministracionPokemonService {

    private final PokemonRepository pokemonRepository;
    private final TipoRepository tipoRepository;
    private final PokemonService pokemonService;

    public AdministracionPokemonService(PokemonRepository pokemonRepository, TipoRepository tipoRepository,
                                        PokemonService pokemonService) {
        this.pokemonRepository = pokemonRepository;
        this.tipoRepository = tipoRepository;
        this.pokemonService = pokemonService;
    }

    /** CA1: registra un Pokémon nuevo y devuelve su ficha. */
    public PokemonDetalleDto registrar(PokemonFormularioDto datos) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (datos.numero() == null) {
            errores.put("numero", "El número es obligatorio");
        }
        List<Tipo> tipos = buscarTipos(datos.tipos(), errores);
        if (!errores.isEmpty()) {
            throw new DatosInvalidosException(errores);
        }
        if (pokemonRepository.existsById(datos.numero())) {
            throw new PokemonDuplicadoException(
                    "Ya existe un Pokémon con el N.º " + String.format("%04d", datos.numero()));
        }
        validarNombreLibre(datos.nombre(), null);

        Pokemon pokemon = new Pokemon(datos.numero(), datos.nombre().trim());
        asignarDatos(pokemon, datos, tipos);
        pokemonRepository.save(pokemon);
        return pokemonService.obtenerPorNumero(pokemon.getNumero());
    }

    /** CA2: edita los datos de un Pokémon existente. El número no se puede cambiar. */
    public PokemonDetalleDto editar(int numero, PokemonFormularioDto datos) {
        Pokemon pokemon = pokemonRepository.findById(numero).orElseThrow(PokemonNoEncontradoException::new);
        Map<String, String> errores = new LinkedHashMap<>();
        if (datos.numero() != null && datos.numero() != numero) {
            errores.put("numero", "El número no se puede cambiar");
        }
        List<Tipo> tipos = buscarTipos(datos.tipos(), errores);
        if (!errores.isEmpty()) {
            throw new DatosInvalidosException(errores);
        }
        validarNombreLibre(datos.nombre(), numero);

        pokemon.setNombre(datos.nombre().trim());
        asignarDatos(pokemon, datos, tipos);
        return pokemonService.obtenerPorNumero(numero);
    }

    /** CA4: elimina un Pokémon. La confirmación la pide el frontend antes de llamar. */
    public void eliminar(int numero) {
        Pokemon pokemon = pokemonRepository.findById(numero).orElseThrow(PokemonNoEncontradoException::new);
        pokemonRepository.delete(pokemon);
    }

    private void asignarDatos(Pokemon pokemon, PokemonFormularioDto datos, List<Tipo> tipos) {
        pokemon.setDescripcion(textoOpcional(datos.descripcion()));
        pokemon.setImagenUrl(Optional.ofNullable(textoOpcional(datos.imagenUrl()))
                .orElse(ConversorPokeApi.urlImagen(pokemon.getNumero())));
        pokemon.setAltura(datos.altura());
        pokemon.setPeso(datos.peso());
        pokemon.setGenero(datos.genero());
        pokemon.setCategoria(datos.categoria().trim());
        pokemon.setHabilidad(datos.habilidad().trim());
        EstadisticasDto e = datos.estadisticas();
        pokemon.setEstadisticas(e.ps(), e.ataque(), e.defensa(), e.ataqueEspecial(), e.defensaEspecial(), e.velocidad());
        pokemon.setTipos(tipos);
        pokemon.setDebilidades(debilidades(tipos));
    }

    /** Busca los tipos por clave; los que no existen o se repiten quedan como error del campo "tipos". */
    private List<Tipo> buscarTipos(List<String> claves, Map<String, String> errores) {
        List<Tipo> tipos = new ArrayList<>();
        if (new HashSet<>(claves).size() < claves.size()) {
            errores.put("tipos", "Los tipos no se pueden repetir");
            return tipos;
        }
        for (String clave : claves) {
            Optional<Tipo> tipo = tipoRepository.findByClave(clave.trim().toLowerCase());
            if (tipo.isEmpty() || !TablaTipos.existe(tipo.get().getClave())) {
                errores.put("tipos", "Tipo desconocido: " + clave);
                return tipos;
            }
            tipos.add(tipo.get());
        }
        return tipos;
    }

    /** Las debilidades se calculan con la tabla de tipos, como en la importación desde PokéAPI. */
    private List<Tipo> debilidades(List<Tipo> tipos) {
        return TablaTipos.debilidades(tipos.stream().map(Tipo::getClave).toList()).stream()
                .map(tipoRepository::findByClave)
                .flatMap(Optional::stream)
                .toList();
    }

    private void validarNombreLibre(String nombre, Integer numeroActual) {
        pokemonRepository.findByNombreIgnoreCase(nombre.trim())
                .filter(otro -> !otro.getNumero().equals(numeroActual))
                .ifPresent(otro -> {
                    throw new PokemonDuplicadoException("Ya existe un Pokémon llamado " + otro.getNombre());
                });
    }

    private static String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
