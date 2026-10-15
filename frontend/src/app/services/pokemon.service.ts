import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { PokemonDetalle, PokemonFormulario, PokemonResumen, Tipo } from '../models/pokemon.model';

/**
 * Acceso a la API de la Pokédex. La app nunca consulta PokéAPI directamente.
 */
@Injectable({ providedIn: 'root' })
export class PokemonService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/pokemon`;

  /**
   * Listado de la Pokédex ordenado por número.
   * @param busqueda nombre (parcial) o número ("1", "001", "0001"); vacío devuelve todos
   */
  listar(busqueda = ''): Observable<PokemonResumen[]> {
    const q = busqueda.trim();
    const params = q ? new HttpParams().set('q', q) : undefined;
    return this.http.get<PokemonResumen[]>(this.url, { params });
  }

  obtenerPorNumero(numero: number): Observable<PokemonDetalle> {
    return this.http.get<PokemonDetalle>(`${this.url}/${numero}`);
  }

  obtenerPorNombre(nombre: string): Observable<PokemonDetalle> {
    return this.http.get<PokemonDetalle>(`${this.url}/nombre/${encodeURIComponent(nombre)}`);
  }

  /** Tipos en el orden oficial, para el formulario. */
  listarTipos(): Observable<Tipo[]> {
    return this.http.get<Tipo[]>(`${environment.apiUrl}/tipos`);
  }

  // HU-13: requieren la sesión de administrador (la agrega authInterceptor)

  registrar(datos: PokemonFormulario): Observable<PokemonDetalle> {
    return this.http.post<PokemonDetalle>(this.url, datos);
  }

  editar(numero: number, datos: PokemonFormulario): Observable<PokemonDetalle> {
    return this.http.put<PokemonDetalle>(`${this.url}/${numero}`, datos);
  }

  eliminar(numero: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${numero}`);
  }
}
