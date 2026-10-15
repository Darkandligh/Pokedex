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

/** Datos de un Pokémon para el listado. */
export interface PokemonResumen {
  numero: number;
  nombre: string;
  imagenUrl: string;
  tipos: Tipo[];
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
  /** Número del Pokémon anterior, o null si es el primero. */
  anterior: number | null;
  /** Número del Pokémon siguiente, o null si es el último. */
  siguiente: number | null;
}

/** Datos para registrar o editar un Pokémon (HU-13). */
export interface PokemonFormulario {
  /** Obligatorio al registrar; al editar no se puede cambiar. */
  numero: number | null;
  nombre: string;
  descripcion: string;
  /** Vacía: se usa la imagen oficial del número. */
  imagenUrl: string;
  altura: number;
  peso: number;
  genero: string;
  categoria: string;
  habilidad: string;
  /** Claves de uno o dos tipos. */
  tipos: string[];
  estadisticas: Estadisticas;
}

/** Noticia de la portada (HU-06). */
export interface Noticia {
  id: number;
  titulo: string;
  resumen: string;
  imagenUrl: string | null;
  /** Fecha de publicación en formato ISO ("2026-10-15"). */
  fecha: string;
}

/** Respuesta de error de la API: {"estado": 400, "mensaje": "...", "errores": {"nombre": "..."}}. */
export interface ErrorApi {
  estado: number;
  mensaje: string;
  /** Solo en los errores de validación: campo → mensaje. */
  errores?: Record<string, string>;
}
