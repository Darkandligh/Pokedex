package co.edu.fet.pokedex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Tipo elemental de un Pokémon (Fuego, Agua, Planta...).
 * La clave es el identificador en inglés que usa PokéAPI ("fire") y sirve
 * para asignar el color en el frontend; el nombre es el texto en español.
 */
@Entity
public class Tipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String clave;

    @Column(nullable = false, length = 50)
    private String nombre;

    protected Tipo() {
    }

    public Tipo(String clave, String nombre) {
        this.clave = clave;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getClave() {
        return clave;
    }

    public String getNombre() {
        return nombre;
    }
}
