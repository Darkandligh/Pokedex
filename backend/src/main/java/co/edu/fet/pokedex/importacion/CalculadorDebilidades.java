package co.edu.fet.pokedex.importacion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calcula las debilidades de un Pokémon a partir de las relaciones de daño de sus tipos.
 *
 * Para cada tipo atacante se multiplican los efectos sobre cada tipo defensor
 * (×2 doble daño, ×0.5 mitad de daño, ×0 sin daño). Es débil a los tipos cuyo
 * multiplicador total es mayor que 1. Así, en un Pokémon de dos tipos, una debilidad
 * de un tipo puede quedar anulada por una resistencia del otro.
 */
public final class CalculadorDebilidades {

    /** Orden oficial de los 18 tipos, usado para presentar las debilidades de forma estable. */
    public static final List<String> TIPOS_OFICIALES = List.of(
            "normal", "fighting", "flying", "poison", "ground", "rock", "bug", "ghost", "steel",
            "fire", "water", "grass", "electric", "psychic", "ice", "dragon", "dark", "fairy");

    /** Relaciones de daño que recibe un tipo defensor (claves de los tipos atacantes). */
    public record RelacionesDano(Set<String> dobleDanoDe, Set<String> mitadDanoDe, Set<String> sinDanoDe) {
    }

    private CalculadorDebilidades() {
    }

    /**
     * @param relacionesPorTipo relaciones de daño de cada tipo del Pokémon
     * @return claves de los tipos a los que es débil, de mayor a menor multiplicador
     */
    public static List<String> calcular(List<RelacionesDano> relacionesPorTipo) {
        Map<String, Double> multiplicadores = new LinkedHashMap<>();
        for (String atacante : TIPOS_OFICIALES) {
            double multiplicador = 1.0;
            for (RelacionesDano defensor : relacionesPorTipo) {
                if (defensor.sinDanoDe().contains(atacante)) {
                    multiplicador *= 0.0;
                } else if (defensor.dobleDanoDe().contains(atacante)) {
                    multiplicador *= 2.0;
                } else if (defensor.mitadDanoDe().contains(atacante)) {
                    multiplicador *= 0.5;
                }
            }
            if (multiplicador > 1.0) {
                multiplicadores.put(atacante, multiplicador);
            }
        }
        List<String> debilidades = new ArrayList<>(multiplicadores.keySet());
        // Orden estable: primero las de mayor multiplicador (×4), luego el orden oficial
        debilidades.sort(Comparator.comparing((String clave) -> -multiplicadores.get(clave))
                .thenComparing(TIPOS_OFICIALES::indexOf));
        return debilidades;
    }
}
