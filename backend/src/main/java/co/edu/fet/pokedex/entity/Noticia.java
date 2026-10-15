package co.edu.fet.pokedex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

/**
 * Noticia que se muestra en la portada (HU-06).
 */
@Entity
public class Noticia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, length = 400)
    private String resumen;

    @Column(length = 300)
    private String imagenUrl;

    @Column(nullable = false)
    private LocalDate fechaPublicacion;

    protected Noticia() {
    }

    public Noticia(String titulo, String resumen, String imagenUrl, LocalDate fechaPublicacion) {
        this.titulo = titulo;
        this.resumen = resumen;
        this.imagenUrl = imagenUrl;
        this.fechaPublicacion = fechaPublicacion;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getResumen() {
        return resumen;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }
}
