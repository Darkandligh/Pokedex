import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonRouterLink,
  IonSearchbar,
  IonSpinner,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { Observable, catchError, distinctUntilChanged, map, of, startWith, switchMap } from 'rxjs';

import { PokemonResumen, Tipo } from '../../models/pokemon.model';
import { PokemonService } from '../../services/pokemon.service';
import { colorTipo } from '../../shared/colores-tipo';
import { NumeroPokedexPipe } from '../../shared/numero-pokedex.pipe';
import { TipoEtiquetaComponent } from '../../shared/tipo-etiqueta.component';

type EstadoListado =
  | { tipo: 'cargando' }
  | { tipo: 'ok'; pokemon: PokemonResumen[] }
  | { tipo: 'error' };

/** Mensaje exacto del criterio de aceptación CA2 de la HU-03. */
export const MENSAJE_SIN_RESULTADOS = 'No se encontraron Pokémon';

/**
 * Listado de la Pokédex (HU-02) con buscador por nombre o número (HU-03) y filtro por tipo (HU-04).
 */
@Component({
  selector: 'app-listado',
  templateUrl: 'listado.page.html',
  styleUrls: ['listado.page.scss'],
  imports: [
    IonButton,
    IonButtons,
    IonContent,
    IonHeader,
    IonRouterLink,
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

  /** HU-04: clave del tipo elegido; vacío = todos. */
  protected readonly tipoElegido = signal('');
  protected readonly tipos = toSignal(this.pokemonService.listarTipos().pipe(catchError(() => of<Tipo[]>([]))), {
    initialValue: [] as Tipo[],
  });
  protected readonly colorTipo = colorTipo;

  /** La búsqueda y el tipo se combinan en una sola consulta a la API. */
  private readonly filtros = computed(() => ({ texto: this.busqueda().trim(), tipo: this.tipoElegido() }));

  protected readonly estado = toSignal(
    toObservable(this.filtros).pipe(
      distinctUntilChanged((antes, ahora) => antes.texto === ahora.texto && antes.tipo === ahora.tipo),
      switchMap(({ texto, tipo }) => this.cargar(texto, tipo)),
    ),
    { initialValue: { tipo: 'cargando' } as EstadoListado },
  );

  protected readonly mensajeSinResultados = MENSAJE_SIN_RESULTADOS;

  protected buscar(evento: Event): void {
    const valor = (evento as CustomEvent<{ value?: string | null }>).detail?.value ?? '';
    this.busqueda.set(valor);
  }

  /** Elegir el tipo que ya está elegido lo quita (vuelve a «Todos»). */
  protected elegirTipo(clave: string): void {
    this.tipoElegido.update((actual) => (actual === clave ? '' : clave));
  }

  private cargar(texto: string, tipo: string): Observable<EstadoListado> {
    return this.pokemonService.listar(texto, tipo).pipe(
      map((pokemon): EstadoListado => ({ tipo: 'ok', pokemon })),
      catchError(() => of<EstadoListado>({ tipo: 'error' })),
      startWith<EstadoListado>({ tipo: 'cargando' }),
    );
  }
}
