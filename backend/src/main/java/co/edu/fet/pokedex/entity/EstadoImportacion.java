package co.edu.fet.pokedex.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Registro (una sola fila) de que la importación desde PokéAPI ya terminó.
 * Así, si un administrador elimina un Pokémon (HU-13), no se vuelve a importar al reiniciar.
 */
@Entity
public class EstadoImportacion {

    public static final int ID = 1;

    @Id
    private Integer id = ID;

    private boolean completada;

    protected EstadoImportacion() {
    }

    public static EstadoImportacion completada() {
        EstadoImportacion estado = new EstadoImportacion();
        estado.completada = true;
        return estado;
    }

    public boolean isCompletada() {
        return completada;
    }
}
