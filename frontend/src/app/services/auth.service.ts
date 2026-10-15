import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';

import { environment } from '../../environments/environment';

interface Sesion {
  usuario: string;
  /** Valor de la cabecera Authorization ("Basic ..."). */
  encabezado: string;
}

/** La sesión dura mientras la pestaña esté abierta. */
const CLAVE_ALMACEN = 'pokedex.admin';

/**
 * Sesión del administrador (HU-13). La API usa HTTP Basic: se comprueban las credenciales
 * contra /api/admin/sesion y, si son correctas, se guardan para las siguientes peticiones.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly sesion = signal<Sesion | null>(leerSesion());

  readonly usuario = computed(() => this.sesion()?.usuario ?? null);
  readonly esAdmin = computed(() => this.sesion() !== null);

  /** Cabecera Authorization para la API, o null si no hay sesión. */
  encabezado(): string | null {
    return this.sesion()?.encabezado ?? null;
  }

  /** Falla con 401 si el usuario o la clave no son de un administrador. */
  ingresar(usuario: string, clave: string): Observable<void> {
    const encabezado = `Basic ${aBase64(`${usuario}:${clave}`)}`;
    return this.http
      .get<{ usuario: string }>(`${environment.apiUrl}/admin/sesion`, { headers: { Authorization: encabezado } })
      .pipe(
        tap((respuesta) => this.guardar({ usuario: respuesta.usuario, encabezado })),
        map(() => undefined),
      );
  }

  salir(): void {
    this.guardar(null);
  }

  private guardar(sesion: Sesion | null): void {
    this.sesion.set(sesion);
    try {
      if (sesion) {
        sessionStorage.setItem(CLAVE_ALMACEN, JSON.stringify(sesion));
      } else {
        sessionStorage.removeItem(CLAVE_ALMACEN);
      }
    } catch {
      // Sin almacenamiento (modo privado): la sesión dura hasta recargar la página
    }
  }
}

function leerSesion(): Sesion | null {
  try {
    const guardada = sessionStorage.getItem(CLAVE_ALMACEN);
    return guardada ? (JSON.parse(guardada) as Sesion) : null;
  } catch {
    return null;
  }
}

/** btoa solo acepta Latin-1: se codifica primero en UTF-8 para admitir tildes y ñ. */
function aBase64(texto: string): string {
  return btoa(String.fromCharCode(...new TextEncoder().encode(texto)));
}
