import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

/** Las páginas de administración (HU-13) piden iniciar sesión y luego vuelven a la página pedida. */
export const adminGuard: CanActivateFn = (_ruta, estado) =>
  inject(AuthService).esAdmin()
    ? true
    : inject(Router).createUrlTree(['/admin/ingresar'], { queryParams: { volver: estado.url } });
