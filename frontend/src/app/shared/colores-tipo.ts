/** Paleta propia por tipo de Pokémon: color de fondo y color de texto legible sobre él. */
export interface ColorTipo {
  fondo: string;
  texto: string;
}

const OSCURO = '#1d1d1f';
const CLARO = '#ffffff';

const COLORES: Record<string, ColorTipo> = {
  normal: { fondo: '#a8a77a', texto: OSCURO },
  fire: { fondo: '#ee8130', texto: OSCURO },
  water: { fondo: '#5b9ce6', texto: OSCURO },
  grass: { fondo: '#6cc04a', texto: OSCURO },
  electric: { fondo: '#f4d23c', texto: OSCURO },
  ice: { fondo: '#86d2d0', texto: OSCURO },
  fighting: { fondo: '#c2412f', texto: CLARO },
  poison: { fondo: '#9a4fb0', texto: CLARO },
  ground: { fondo: '#dbb55e', texto: OSCURO },
  flying: { fondo: '#a3b8f0', texto: OSCURO },
  psychic: { fondo: '#f2668b', texto: OSCURO },
  bug: { fondo: '#a6b91a', texto: OSCURO },
  rock: { fondo: '#bfaa5a', texto: OSCURO },
  ghost: { fondo: '#6a5596', texto: CLARO },
  dragon: { fondo: '#6c3df0', texto: CLARO },
  dark: { fondo: '#5a4a40', texto: CLARO },
  steel: { fondo: '#b7b7ce', texto: OSCURO },
  fairy: { fondo: '#e9a5cf', texto: OSCURO },
};

export function colorTipo(clave: string | undefined): ColorTipo {
  return (clave && COLORES[clave]) || { fondo: '#9e9e9e', texto: OSCURO };
}
