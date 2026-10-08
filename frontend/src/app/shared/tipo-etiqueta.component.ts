import { Component, computed, input } from '@angular/core';

import { Tipo } from '../models/pokemon.model';
import { colorTipo } from './colores-tipo';

/** Etiqueta de un tipo de Pokémon con su color. */
@Component({
  selector: 'app-tipo-etiqueta',
  template: `<span class="etiqueta" [style.background]="color().fondo" [style.color]="color().texto">{{ tipo().nombre }}</span>`,
  styles: `
    .etiqueta {
      display: inline-block;
      min-width: 76px;
      padding: 3px 12px;
      border-radius: 999px;
      font-size: 0.8rem;
      font-weight: 600;
      text-align: center;
      letter-spacing: 0.02em;
    }
  `,
})
export class TipoEtiquetaComponent {
  readonly tipo = input.required<Tipo>();
  protected readonly color = computed(() => colorTipo(this.tipo().clave));
}
