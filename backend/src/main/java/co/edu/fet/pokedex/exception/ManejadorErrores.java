package co.edu.fet.pokedex.exception;

import co.edu.fet.pokedex.dto.ErrorDto;
import co.edu.fet.pokedex.dto.ErrorValidacionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo centralizado de errores: todas las respuestas de error tienen la forma
 * {"estado": 404, "mensaje": "..."}. Los errores de validación agregan "errores" (campo → mensaje).
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);
    private static final String DATOS_INVALIDOS = "Datos inválidos";

    @ExceptionHandler(PokemonNoEncontradoException.class)
    public ResponseEntity<ErrorDto> pokemonNoEncontrado(PokemonNoEncontradoException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** Un número que no es numérico (por ejemplo /api/pokemon/abc) tampoco corresponde a ningún Pokémon. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return respuesta(HttpStatus.NOT_FOUND, PokemonNoEncontradoException.MENSAJE);
    }

    /** HU-13: campos que no cumplen las validaciones del formulario. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorValidacionDto> camposInvalidos(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return validacion(errores);
    }

    /** HU-13: validaciones que dependen de la base (por ejemplo, un tipo que no existe). */
    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorValidacionDto> datosInvalidos(DatosInvalidosException e) {
        return validacion(e.getErrores());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> cuerpoIlegible(HttpMessageNotReadableException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido");
    }

    /** HU-13: número o nombre repetido. */
    @ExceptionHandler(PokemonDuplicadoException.class)
    public ResponseEntity<ErrorDto> pokemonDuplicado(PokemonDuplicadoException e) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorDto> metodoNoPermitido(HttpRequestMethodNotSupportedException e) {
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido");
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

    private static ResponseEntity<ErrorValidacionDto> validacion(Map<String, String> errores) {
        return ResponseEntity.badRequest()
                .body(new ErrorValidacionDto(HttpStatus.BAD_REQUEST.value(), DATOS_INVALIDOS, errores));
    }
}
