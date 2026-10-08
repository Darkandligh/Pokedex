import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { IonContent, IonHeader, IonSpinner, IonTitle, IonToolbar } from '@ionic/angular';
import { Observable, catchError, map, of, startWith } from 'rxjs';

import { PokemonResumen } from '../../models/pokemon.model';
import { PokemonService } from '../../services/pokemon.service';
import { NumeroPokedexPipe } from '../../shared/numero-pokedex.pipe';
import { TipoEtiquetaComponent } from '../../shared/tipo-etiqueta.component';

type EstadoListado =
  | { tipo: 'cargando' }
  | { tipo: 'ok'; pokemon: PokemonResumen[] }
  | { tipo: 'error' };

/**
 * Listado de la Pokédex (HU-02): Pokémon ordenados por número; al seleccionar uno se abre su ficha.
 */
@Component({
  selector: 'app-listado',
  templateUrl: 'listado.page.html',
  styleUrls: ['listado.page.scss'],
  imports: [IonContent, IonHeader, IonSpinner, IonTitle, IonToolbar, NumeroPokedexPipe, RouterLink, TipoEtiquetaComponent],
})
export class ListadoPage {
  private readonly pokemonService = inject(PokemonService);

  protected readonly estado = toSignal(this.cargar(), { initialValue: { tipo: 'cargando' } as EstadoListado });

  private cargar(): Observable<EstadoListado> {
    return this.pokemonService.listar().pipe(
      map((pokemon): EstadoListado => ({ tipo: 'ok', pokemon })),
      catchError(() => of<EstadoListado>({ tipo: 'error' })),
      startWith<EstadoListado>({ tipo: 'cargando' }),
    );
  }
}
