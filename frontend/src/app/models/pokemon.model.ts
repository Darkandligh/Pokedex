// Modelos que reflejan los DTOs de la API de la Pokédex.

export interface Tipo {
  /** Identificador del tipo ("fire"), se usa para el color. */
  clave: string;
  /** Nombre en español ("Fuego"). */
  nombre: string;
}

export interface Estadisticas {
  ps: number;
  ataque: number;
  defensa: number;
  ataqueEspecial: number;
  defensaEspecial: number;
  velocidad: number;
}

export interface Evolucion {
  numero: number;
  nombre: string;
  imagenUrl: string;
  /** 0 = forma base, 1 = primera evolución, 2 = segunda evolución. */
  etapa: number;
  /** true si el Pokémon está en la Pokédex y se puede abrir su ficha. */
  disponible: boolean;
}

export interface PokemonDetalle {
  numero: number;
  nombre: string;
  descripcion: string;
  imagenUrl: string;
  /** Metros. */
  altura: number;
  /** Kilogramos. */
  peso: number;
  genero: string;
  categoria: string;
  habilidad: string;
  tipos: Tipo[];
  debilidades: Tipo[];
  estadisticas: Estadisticas;
  cadenaEvolutiva: Evolucion[];
}
