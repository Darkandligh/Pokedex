package co.edu.fet.pokedex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * Pokémon de la Pokédex. El número de la Pokédex nacional es la llave primaria.
 */
@Entity
public class Pokemon {

    @Id
    private Integer numero;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(length = 300)
    private String imagenUrl;

    /** Altura en metros. */
    private Double altura;

    /** Peso en kilogramos. */
    private Double peso;

    @Column(length = 50)
    private String genero;

    @Column(length = 80)
    private String categoria;

    @Column(length = 80)
    private String habilidad;

    // Estadísticas base
    private Integer ps;
    private Integer ataque;
    private Integer defensa;
    private Integer ataqueEspecial;
    private Integer defensaEspecial;
    private Integer velocidad;

    /** Tipos en el orden oficial (primario, secundario). */
    @ManyToMany
    @JoinTable(name = "pokemon_tipos",
            joinColumns = @JoinColumn(name = "pokemon_numero"),
            inverseJoinColumns = @JoinColumn(name = "tipo_id"))
    @OrderColumn(name = "posicion")
    private List<Tipo> tipos = new ArrayList<>();

    /** Tipos contra los que este Pokémon recibe daño aumentado. */
    @ManyToMany
    @JoinTable(name = "pokemon_debilidades",
            joinColumns = @JoinColumn(name = "pokemon_numero"),
            inverseJoinColumns = @JoinColumn(name = "tipo_id"))
    @OrderColumn(name = "posicion")
    private List<Tipo> debilidades = new ArrayList<>();

    /** Cadena evolutiva a la que pertenece (ver {@link Evolucion#getCadenaId()}). */
    private Integer cadenaEvolutivaId;

    protected Pokemon() {
    }

    public Pokemon(Integer numero, String nombre) {
        this.numero = numero;
        this.nombre = nombre;
    }

    public Integer getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Double getAltura() {
        return altura;
    }

    public void setAltura(Double altura) {
        this.altura = altura;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getHabilidad() {
        return habilidad;
    }

    public void setHabilidad(String habilidad) {
        this.habilidad = habilidad;
    }

    public Integer getPs() {
        return ps;
    }

    public Integer getAtaque() {
        return ataque;
    }

    public Integer getDefensa() {
        return defensa;
    }

    public Integer getAtaqueEspecial() {
        return ataqueEspecial;
    }

    public Integer getDefensaEspecial() {
        return defensaEspecial;
    }

    public Integer getVelocidad() {
        return velocidad;
    }

    /** Asigna las 6 estadísticas base en el orden oficial. */
    public void setEstadisticas(int ps, int ataque, int defensa, int ataqueEspecial, int defensaEspecial, int velocidad) {
        this.ps = ps;
        this.ataque = ataque;
        this.defensa = defensa;
        this.ataqueEspecial = ataqueEspecial;
        this.defensaEspecial = defensaEspecial;
        this.velocidad = velocidad;
    }

    public List<Tipo> getTipos() {
        return tipos;
    }

    public void setTipos(List<Tipo> tipos) {
        this.tipos = new ArrayList<>(tipos);
    }

    public List<Tipo> getDebilidades() {
        return debilidades;
    }

    public void setDebilidades(List<Tipo> debilidades) {
        this.debilidades = new ArrayList<>(debilidades);
    }

    public Integer getCadenaEvolutivaId() {
        return cadenaEvolutivaId;
    }

    public void setCadenaEvolutivaId(Integer cadenaEvolutivaId) {
        this.cadenaEvolutivaId = cadenaEvolutivaId;
    }
}
