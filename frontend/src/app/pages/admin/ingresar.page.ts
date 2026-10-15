import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { IonBackButton, IonButtons, IonContent, IonHeader, IonTitle, IonToolbar } from '@ionic/angular';
import { finalize } from 'rxjs';

import { AuthService } from '../../services/auth.service';

/** Mensaje cuando el usuario o la clave no son de un administrador (HU-13 CA5). */
export const MENSAJE_CREDENCIALES_INCORRECTAS = 'Usuario o clave incorrectos';

/**
 * Inicio de sesión del administrador (HU-13).
 */
@Component({
  selector: 'app-ingresar',
  templateUrl: 'ingresar.page.html',
  styleUrls: ['admin.scss'],
  imports: [IonBackButton, IonButtons, IonContent, IonHeader, IonTitle, IonToolbar, ReactiveFormsModule],
})
export class IngresarPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Página a la que se vuelve después de iniciar sesión (?volver=/admin/...). */
  readonly volver = input<string>();

  protected readonly formulario = inject(FormBuilder).nonNullable.group({
    usuario: ['', Validators.required],
    clave: ['', Validators.required],
  });
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected ingresar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const { usuario, clave } = this.formulario.getRawValue();
    this.enviando.set(true);
    this.error.set(null);
    this.auth
      .ingresar(usuario.trim(), clave)
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: () => void this.router.navigateByUrl(this.destino(), { replaceUrl: true }),
        error: (error: unknown) =>
          this.error.set(
            error instanceof HttpErrorResponse && error.status === 401
              ? MENSAJE_CREDENCIALES_INCORRECTAS
              : 'No se pudo conectar con la API de la Pokédex.',
          ),
      });
  }

  /** Solo se vuelve a páginas de la administración, nunca a otra dirección. */
  private destino(): string {
    const volver = this.volver();
    return volver?.startsWith('/admin') && !volver.startsWith('/admin/ingresar') ? volver : '/admin';
  }
}
