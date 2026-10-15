package co.edu.fet.pokedex.importacion;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * La tabla de tipos debe dar las mismas debilidades que PokéAPI (las de datos-fijos.json).
 */
class TablaTiposTest {

    @Test
    void bulbasaurPlantaVeneno() {
        assertThat(TablaTipos.debilidades(List.of("grass", "poison")))
                .containsExactly("flying", "fire", "psychic", "ice");
    }

    @Test
    void charizardFuegoVoladorEsDobleDebilARoca() {
        assertThat(TablaTipos.debilidades(List.of("fire", "flying")))
                .containsExactly("rock", "water", "electric");
    }

    @Test
    void squirtleAgua() {
        assertThat(TablaTipos.debilidades(List.of("water"))).containsExactly("grass", "electric");
    }

    @Test
    void lasInmunidadesAnulanLaDebilidad() {
        // Gengar (Fantasma/Veneno) es inmune a Normal y Lucha
        assertThat(TablaTipos.debilidades(List.of("ghost", "poison")))
                .containsExactly("ground", "ghost", "psychic", "dark");
    }

    @Test
    void tipoDesconocido() {
        assertThat(TablaTipos.existe("luz")).isFalse();
        assertThatThrownBy(() -> TablaTipos.debilidades(List.of("luz")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
