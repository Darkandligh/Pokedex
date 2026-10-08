package co.edu.fet.pokedex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Eslabón de una cadena evolutiva.
 * Todos los eslabones de una misma cadena comparten el mismo cadenaId; la etapa
 * indica la profundidad (0 = forma base, 1 = primera evolución...) y el orden
 * permite mostrar las ramificaciones (por ejemplo, las evoluciones de Eevee).
 * Una cadena puede incluir Pokémon que no estén importados en la Pokédex.
 */
@Entity
@Table(indexes = @Index(name = "idx_evolucion_cadena", columnList = "cadenaId"))
public class Evolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer cadenaId;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 300)
    private String imagenUrl;

    @Column(nullable = false)
    private Integer etapa;

    @Column(nullable = false)
    private Integer orden;

    protected Evolucion() {
    }

    public Evolucion(Integer cadenaId, Integer numero, String nombre, String imagenUrl, Integer etapa, Integer orden) {
        this.cadenaId = cadenaId;
        this.numero = numero;
        this.nombre = nombre;
        this.imagenUrl = imagenUrl;
        this.etapa = etapa;
        this.orden = orden;
    }

    public Long getId() {
        return id;
    }

    public Integer getCadenaId() {
        return cadenaId;
    }

    public Integer getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public Integer getEtapa() {
        return etapa;
    }

    public Integer getOrden() {
        return orden;
    }
}
