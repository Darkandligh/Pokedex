import { Component, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { IonContent, IonHeader, IonSearchbar, IonSpinner, IonTitle, IonToolbar } from '@ionic/angular';
import { Observable, catchError, distinctUntilChanged, map, of, startWith, switchMap } from 'rxjs';

import { PokemonResumen } from '../../models/pokemon.model';
import { PokemonService } from '../../services/pokemon.service';
import { NumeroPokedexPipe } from '../../shared/numero-pokedex.pipe';
import { TipoEtiquetaComponent } from '../../shared/tipo-etiqueta.component';

type EstadoListado =
  | { tipo: 'cargando' }
  | { tipo: 'ok'; pokemon: PokemonResumen[] }
  | { tipo: 'error' };

/** Mensaje exacto del criterio de aceptación CA2 de la HU-03. */
export const MENSAJE_SIN_RESULTADOS = 'No se encontraron Pokémon';

/**
 * Listado de la Pokédex (HU-02) con buscador por nombre o número (HU-03).
 */
@Component({
  selector: 'app-listado',
  templateUrl: 'listado.page.html',
  styleUrls: ['listado.page.scss'],
  imports: [
    IonContent,
    IonHeader,
    IonSearchbar,
    IonSpinner,
    IonTitle,
    IonToolbar,
    NumeroPokedexPipe,
    RouterLink,
    TipoEtiquetaComponent,
  ],
})
export class ListadoPage {
  private readonly pokemonService = inject(PokemonService);

  /** Texto del buscador (el ion-searchbar ya aplica la espera entre teclas). */
  protected readonly busqueda = signal('');

  protected readonly estado = toSignal(
    toObservable(this.busqueda).pipe(
      map((texto) => texto.trim()),
      distinctUntilChanged(),
      switchMap((texto) => this.cargar(texto)),
    ),
    { initialValue: { tipo: 'cargando' } as EstadoListado },
  );

  protected readonly mensajeSinResultados = MENSAJE_SIN_RESULTADOS;

  protected buscar(evento: Event): void {
    const valor = (evento as CustomEvent<{ value?: string | null }>).detail?.value ?? '';
    this.busqueda.set(valor);
  }

  private cargar(texto: string): Observable<EstadoListado> {
    return this.pokemonService.listar(texto).pipe(
      map((pokemon): EstadoListado => ({ tipo: 'ok', pokemon })),
      catchError(() => of<EstadoListado>({ tipo: 'error' })),
      startWith<EstadoListado>({ tipo: 'cargando' }),
    );
  }
}
