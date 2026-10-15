import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import {
  AlertController,
  IonBackButton,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonRouterLinkWithHref,
  IonSpinner,
  IonTitle,
  IonToolbar,
  ViewWillEnter,
} from '@ionic/angular';

import { PokemonResumen } from '../../models/pokemon.model';
import { AuthService } from '../../services/auth.service';
import { PokemonService } from '../../services/pokemon.service';
import { NumeroPokedexPipe } from '../../shared/numero-pokedex.pipe';
import { TipoEtiquetaComponent } from '../../shared/tipo-etiqueta.component';

type EstadoPanel =
  | { tipo: 'cargando' }
  | { tipo: 'ok'; pokemon: PokemonResumen[] }
  | { tipo: 'error' };

/** Rol del botón que confirma la eliminación en la alerta. */
export const ROL_CONFIRMAR = 'destructive';

/**
 * Panel del administrador (HU-13): registrar, editar y eliminar Pokémon.
 */
@Component({
  selector: 'app-panel',
  templateUrl: 'panel.page.html',
  styleUrls: ['admin.scss', 'panel.page.scss'],
  imports: [
    IonBackButton,
    IonButton,
    IonButtons,
    IonContent,
    IonHeader,
    IonRouterLinkWithHref,
    IonSpinner,
    IonTitle,
    IonToolbar,
    NumeroPokedexPipe,
    RouterLink,
    TipoEtiquetaComponent,
  ],
})
export class PanelPage implements ViewWillEnter {
  private readonly pokemonService = inject(PokemonService);
  private readonly alertas = inject(AlertController);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly estado = signal<EstadoPanel>({ tipo: 'cargando' });
  protected readonly aviso = signal<{ texto: string; error: boolean } | null>(null);
  /** Número del Pokémon que se está eliminando. */
  protected readonly eliminando = signal<number | null>(null);

  /** Ionic conserva la página: se recarga cada vez que se vuelve a ella (por ejemplo, después de editar). */
  ionViewWillEnter(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.pokemonService.listar().subscribe({
      next: (pokemon) => this.estado.set({ tipo: 'ok', pokemon }),
      error: () => this.estado.set({ tipo: 'error' }),
    });
  }

  /** CA4: pide confirmación antes de eliminar. */
  protected async eliminar(pokemon: PokemonResumen): Promise<void> {
    const alerta = await this.alertas.create({
      header: 'Eliminar Pokémon',
      message: `¿Seguro que quieres eliminar a ${pokemon.nombre}? Esta acción no se puede deshacer.`,
      buttons: [
        { text: 'Cancelar', role: 'cancel' },
        { text: 'Eliminar', role: ROL_CONFIRMAR },
      ],
    });
    await alerta.present();
    const { role } = await alerta.onDidDismiss();
    if (role !== ROL_CONFIRMAR) {
      return;
    }

    this.eliminando.set(pokemon.numero);
    this.pokemonService.eliminar(pokemon.numero).subscribe({
      next: () => {
        this.eliminando.set(null);
        this.aviso.set({ texto: `Se eliminó a ${pokemon.nombre}.`, error: false });
        this.cargar();
      },
      error: () => {
        this.eliminando.set(null);
        this.aviso.set({ texto: `No se pudo eliminar a ${pokemon.nombre}.`, error: true });
      },
    });
  }

  protected salir(): void {
    this.auth.salir();
    void this.router.navigateByUrl('/pokedex', { replaceUrl: true });
  }
}
