import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

/**
 * Agrega las credenciales del administrador a las peticiones que las necesitan (HU-13):
 * registrar, editar, eliminar y /api/admin. Las consultas siguen yendo sin credenciales.
 * Si la API responde 401, la sesión guardada ya no sirve y se cierra.
 */
export const authInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  const auth = inject(AuthService);
  const encabezado = auth.encabezado();
  const necesitaSesion =
    peticion.url.startsWith(environment.apiUrl) &&
    (peticion.method !== 'GET' || peticion.url.startsWith(`${environment.apiUrl}/admin/`));

  if (!encabezado || !necesitaSesion || peticion.headers.has('Authorization')) {
    return siguiente(peticion);
  }
  return siguiente(peticion.clone({ setHeaders: { Authorization: encabezado } })).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        auth.salir();
      }
      return throwError(() => error);
    }),
  );
};
