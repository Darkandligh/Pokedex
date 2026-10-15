package co.edu.fet.pokedex.exception;

/**
 * Se lanza al registrar o editar un Pokémon con un número o nombre que ya usa otro (409).
 */
public class PokemonDuplicadoException extends RuntimeException {

    public PokemonDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
