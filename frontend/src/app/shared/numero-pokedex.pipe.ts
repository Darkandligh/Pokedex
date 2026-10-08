import { Pipe, PipeTransform } from '@angular/core';

/** Muestra el número de la Pokédex con cuatro cifras: 1 → "N.º 0001". */
@Pipe({ name: 'numeroPokedex' })
export class NumeroPokedexPipe implements PipeTransform {
  transform(numero: number | null | undefined): string {
    if (numero == null) {
      return '';
    }
    return `N.º ${String(numero).padStart(4, '0')}`;
  }
}
