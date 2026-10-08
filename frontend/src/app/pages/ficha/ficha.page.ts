import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, input } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import {
  IonBackButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonSpinner,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { Observable, catchError, map, of, startWith, switchMap } from 'rxjs';

import { PokemonDetalle } from '../../models/pokemon.model';
import { PokemonService } from '../../services/pokemon.service';
import { colorTipo } from '../../shared/colores-tipo';
import { NumeroPokedexPipe } from '../../shared/numero-pokedex.pipe';
import { TipoEtiquetaComponent } from '../../shared/tipo-etiqueta.component';

type EstadoFicha =
  | { tipo: 'cargando' }
  | { tipo: 'ok'; pokemon: PokemonDetalle }
  | { tipo: 'error' };

/** Valor máximo de referencia para dibujar las barras de estadísticas. */
const MAXIMO_ESTADISTICA = 255;

/**
 * Ficha de un Pokémon (HU-01). La ruta acepta número (/pokemon/1, /pokemon/0001)
 * o nombre (/pokemon/bulbasaur).
 */
@Component({
  selector: 'app-ficha',
  templateUrl: 'ficha.page.html',
  styleUrls: ['ficha.page.scss'],
  imports: [
    DecimalPipe,
    IonBackButton,
    IonButtons,
    IonContent,
    IonHeader,
    IonSpinner,
    IonTitle,
    IonToolbar,
    NumeroPokedexPipe,
    TipoEtiquetaComponent,
  ],
})
export class FichaPage {
  private readonly pokemonService = inject(PokemonService);

  /** Parámetro :id de la ruta. */
  readonly id = input.required<string>();

  protected readonly estado = toSignal(
    toObservable(this.id).pipe(switchMap((id) => this.cargar(id))),
    { initialValue: { tipo: 'cargando' } as EstadoFicha },
  );

  protected readonly pokemon = computed(() => {
    const estado = this.estado();
    return estado.tipo === 'ok' ? estado.pokemon : null;
  });

  /** Color del tipo principal, para el fondo de la imagen. */
  protected readonly colorPrincipal = computed(() => colorTipo(this.pokemon()?.tipos[0]?.clave).fondo);

  protected readonly estadisticas = computed(() => {
    const e = this.pokemon()?.estadisticas;
    if (!e) {
      return [];
    }
    return [
      { nombre: 'PS', valor: e.ps },
      { nombre: 'Ataque', valor: e.ataque },
      { nombre: 'Defensa', valor: e.defensa },
      { nombre: 'Ataque Especial', valor: e.ataqueEspecial },
      { nombre: 'Defensa Especial', valor: e.defensaEspecial },
      { nombre: 'Velocidad', valor: e.velocidad },
    ].map((estadistica) => ({
      ...estadistica,
      porcentaje: Math.min(100, (estadistica.valor / MAXIMO_ESTADISTICA) * 100),
    }));
  });

  private cargar(id: string): Observable<EstadoFicha> {
    const texto = (id ?? '').trim();
    const consulta = /^\d+$/.test(texto)
      ? this.pokemonService.obtenerPorNumero(Number(texto))
      : this.pokemonService.obtenerPorNombre(texto);
    return consulta.pipe(
      map((pokemon): EstadoFicha => ({ tipo: 'ok', pokemon })),
      catchError(() => of<EstadoFicha>({ tipo: 'error' })),
      startWith<EstadoFicha>({ tipo: 'cargando' }),
    );
  }
}
