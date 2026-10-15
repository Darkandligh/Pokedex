package co.edu.fet.pokedex.exception;

import java.util.Map;

/**
 * Validaciones que dependen de la base de datos o de varios campos a la vez
 * (por ejemplo, un tipo que no existe). Se responde 400 igual que las validaciones de campos.
 */
public class DatosInvalidosException extends RuntimeException {

    private final Map<String, String> errores;

    public DatosInvalidosException(Map<String, String> errores) {
        super("Datos inválidos");
        this.errores = Map.copyOf(errores);
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
