package co.edu.fet.pokedex.importacion;

import co.edu.fet.pokedex.importacion.CalculadorDebilidades.RelacionesDano;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadorDebilidadesTest {

    // Relaciones de daño reales (PokéAPI) de los tipos usados en las pruebas
    private static final RelacionesDano PLANTA = new RelacionesDano(
            Set.of("flying", "poison", "bug", "fire", "ice"),
            Set.of("ground", "water", "grass", "electric"),
            Set.of());
    private static final RelacionesDano VENENO = new RelacionesDano(
            Set.of("ground", "psychic"),
            Set.of("fighting", "poison", "bug", "grass", "fairy"),
            Set.of());
    private static final RelacionesDano FUEGO = new RelacionesDano(
            Set.of("ground", "rock", "water"),
            Set.of("bug", "steel", "fire", "grass", "ice", "fairy"),
            Set.of());
    private static final RelacionesDano VOLADOR = new RelacionesDano(
            Set.of("rock", "electric", "ice"),
            Set.of("fighting", "bug", "grass"),
            Set.of("ground"));

    @Test
    void unSoloTipoDevuelveSusDebilidadesEnOrdenOficial() {
        assertThat(CalculadorDebilidades.calcular(List.of(FUEGO)))
                .containsExactly("ground", "rock", "water");
    }

    @Test
    void dosTiposSeCombinanYLasResistenciasAnulanDebilidades() {
        // Bulbasaur (Planta/Veneno): Veneno, Bicho y Tierra quedan neutralizados
        assertThat(CalculadorDebilidades.calcular(List.of(PLANTA, VENENO)))
                .containsExactly("flying", "fire", "psychic", "ice");
    }

    @Test
    void lasInmunidadesEliminanLaDebilidadYLasDoblesVanPrimero() {
        // Charizard (Fuego/Volador): Roca ×4 primero; Tierra ×0 por ser Volador
        assertThat(CalculadorDebilidades.calcular(List.of(FUEGO, VOLADOR)))
                .containsExactly("rock", "water", "electric")
                .doesNotContain("ground");
    }
}
