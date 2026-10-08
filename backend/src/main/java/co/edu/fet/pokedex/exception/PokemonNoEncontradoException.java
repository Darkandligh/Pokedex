package co.edu.fet.pokedex.exception;

/**
 * Se lanza cuando se pide la ficha de un Pokémon que no existe en la Pokédex.
 */
public class PokemonNoEncontradoException extends RuntimeException {

    public static final String MENSAJE = "Pokémon no encontrado";

    public PokemonNoEncontradoException() {
        super(MENSAJE);
    }
}
