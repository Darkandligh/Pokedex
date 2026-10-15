package co.edu.fet.pokedex.importacion;

import co.edu.fet.pokedex.importacion.CalculadorDebilidades.RelacionesDano;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tabla de daño entre tipos (generación 6 en adelante), desde el punto de vista del defensor.
 * Es la misma información que entrega PokéAPI en damage_relations; se guarda aquí para poder
 * calcular las debilidades de un Pokémon registrado por un administrador sin consultar PokéAPI.
 */
public final class TablaTipos {

    private static final Map<String, RelacionesDano> RELACIONES = Map.ofEntries(
            relacion("normal", Set.of("fighting"), Set.of(), Set.of("ghost")),
            relacion("fighting", Set.of("flying", "psychic", "fairy"), Set.of("rock", "bug", "dark"), Set.of()),
            relacion("flying", Set.of("rock", "electric", "ice"), Set.of("fighting", "bug", "grass"), Set.of("ground")),
            relacion("poison", Set.of("ground", "psychic"), Set.of("fighting", "poison", "bug", "grass", "fairy"), Set.of()),
            relacion("ground", Set.of("water", "grass", "ice"), Set.of("poison", "rock"), Set.of("electric")),
            relacion("rock", Set.of("fighting", "ground", "steel", "water", "grass"),
                    Set.of("normal", "flying", "poison", "fire"), Set.of()),
            relacion("bug", Set.of("flying", "rock", "fire"), Set.of("fighting", "ground", "grass"), Set.of()),
            relacion("ghost", Set.of("ghost", "dark"), Set.of("poison", "bug"), Set.of("normal", "fighting")),
            relacion("steel", Set.of("fighting", "ground", "fire"),
                    Set.of("normal", "flying", "rock", "bug", "steel", "grass", "psychic", "ice", "dragon", "fairy"),
                    Set.of("poison")),
            relacion("fire", Set.of("ground", "rock", "water"), Set.of("bug", "steel", "fire", "grass", "ice", "fairy"), Set.of()),
            relacion("water", Set.of("grass", "electric"), Set.of("steel", "fire", "water", "ice"), Set.of()),
            relacion("grass", Set.of("flying", "poison", "bug", "fire", "ice"), Set.of("ground", "water", "grass", "electric"), Set.of()),
            relacion("electric", Set.of("ground"), Set.of("flying", "steel", "electric"), Set.of()),
            relacion("psychic", Set.of("bug", "ghost", "dark"), Set.of("fighting", "psychic"), Set.of()),
            relacion("ice", Set.of("fighting", "rock", "steel", "fire"), Set.of("ice"), Set.of()),
            relacion("dragon", Set.of("ice", "dragon", "fairy"), Set.of("fire", "water", "grass", "electric"), Set.of()),
            relacion("dark", Set.of("fighting", "bug", "fairy"), Set.of("ghost", "dark"), Set.of("psychic")),
            relacion("fairy", Set.of("poison", "steel"), Set.of("fighting", "bug", "dark"), Set.of("dragon")));

    private TablaTipos() {
    }

    /**
     * Debilidades de un Pokémon con los tipos dados.
     *
     * @param clavesTipos claves de sus tipos ("grass", "poison")
     * @return claves de los tipos a los que es débil, de mayor a menor multiplicador
     */
    public static List<String> debilidades(List<String> clavesTipos) {
        return CalculadorDebilidades.calcular(clavesTipos.stream().map(TablaTipos::relaciones).toList());
    }

    public static boolean existe(String clave) {
        return RELACIONES.containsKey(clave);
    }

    private static RelacionesDano relaciones(String clave) {
        RelacionesDano relaciones = RELACIONES.get(clave);
        if (relaciones == null) {
            throw new IllegalArgumentException("Tipo desconocido: " + clave);
        }
        return relaciones;
    }

    private static Map.Entry<String, RelacionesDano> relacion(String defensor, Set<String> dobleDanoDe,
                                                             Set<String> mitadDanoDe, Set<String> sinDanoDe) {
        return Map.entry(defensor, new RelacionesDano(dobleDanoDe, mitadDanoDe, sinDanoDe));
    }
}
