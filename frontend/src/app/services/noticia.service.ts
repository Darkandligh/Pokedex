import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Noticia } from '../models/pokemon.model';

/** Noticias de la portada (HU-06). */
@Injectable({ providedIn: 'root' })
export class NoticiaService {
  private readonly http = inject(HttpClient);

  /** Las más recientes primero. */
  recientes(limite = 6): Observable<Noticia[]> {
    return this.http.get<Noticia[]>(`${environment.apiUrl}/noticias`, {
      params: new HttpParams().set('limite', limite),
    });
  }
}
