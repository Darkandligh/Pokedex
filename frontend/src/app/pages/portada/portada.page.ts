import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { IonContent, IonHeader, IonRouterLinkWithHref, IonSpinner, IonTitle, IonToolbar } from '@ionic/angular';
import { catchError, map, of, startWith } from 'rxjs';

import { Noticia } from '../../models/pokemon.model';
import { NoticiaService } from '../../services/noticia.service';

type EstadoNoticias =
  | { tipo: 'cargando' }
  | { tipo: 'ok'; noticias: Noticia[] }
  | { tipo: 'error' };

/** Mensaje del criterio de aceptación CA3 de la HU-06. */
export const MENSAJE_SIN_NOTICIAS = 'No hay noticias por ahora';

/**
 * Portada (HU-06): noticias recientes y acceso a la Pokédex.
 */
@Component({
  selector: 'app-portada',
  templateUrl: 'portada.page.html',
  styleUrls: ['portada.page.scss'],
  imports: [DatePipe, IonContent, IonHeader, IonRouterLinkWithHref, IonSpinner, IonTitle, IonToolbar, RouterLink],
})
export class PortadaPage {
  private readonly noticiaService = inject(NoticiaService);

  protected readonly estado = toSignal(
    this.noticiaService.recientes().pipe(
      map((noticias): EstadoNoticias => ({ tipo: 'ok', noticias })),
      catchError(() => of<EstadoNoticias>({ tipo: 'error' })),
      startWith<EstadoNoticias>({ tipo: 'cargando' }),
    ),
    { initialValue: { tipo: 'cargando' } as EstadoNoticias },
  );

  protected readonly mensajeSinNoticias = MENSAJE_SIN_NOTICIAS;
}
