package co.edu.fet.pokedex.exception;

import co.edu.fet.pokedex.dto.ErrorDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Manejo centralizado de errores: todas las respuestas de error tienen la forma
 * {"estado": 404, "mensaje": "..."}.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(PokemonNoEncontradoException.class)
    public ResponseEntity<ErrorDto> pokemonNoEncontrado(PokemonNoEncontradoException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** Un número que no es numérico (por ejemplo /api/pokemon/abc) tampoco corresponde a ningún Pokémon. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return respuesta(HttpStatus.NOT_FOUND, PokemonNoEncontradoException.MENSAJE);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDto> rutaInexistente(NoResourceFoundException e) {
        return respuesta(HttpStatus.NOT_FOUND, "Recurso no encontrado");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> errorInesperado(Exception e) {
        log.error("Error inesperado", e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado");
    }

    private static ResponseEntity<ErrorDto> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorDto(estado.value(), mensaje));
    }
}
