import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import {
  IonBackButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonSpinner,
  IonTitle,
  IonToolbar,
  NavController,
  ToastController,
} from '@ionic/angular';
import { catchError, finalize, of } from 'rxjs';

import { ErrorApi, PokemonDetalle, PokemonFormulario } from '../../models/pokemon.model';
import { PokemonService } from '../../services/pokemon.service';
import { NumeroPokedexPipe } from '../../shared/numero-pokedex.pipe';

export const GENEROS = ['Macho y hembra', 'Macho', 'Hembra', 'Desconocido'];

/** Las 6 estadísticas en el orden oficial, con su nombre para mostrar. */
export const ESTADISTICAS = [
  { clave: 'ps', nombre: 'PS' },
  { clave: 'ataque', nombre: 'Ataque' },
  { clave: 'defensa', nombre: 'Defensa' },
  { clave: 'ataqueEspecial', nombre: 'Ataque Especial' },
  { clave: 'defensaEspecial', nombre: 'Defensa Especial' },
  { clave: 'velocidad', nombre: 'Velocidad' },
] as const;

/** Mensaje general cuando hay campos inválidos (HU-13 CA3). */
export const MENSAJE_REVISAR_CAMPOS = 'Revisa los campos marcados';

/** El segundo tipo es opcional, pero no puede repetir el primero. */
const tiposDistintos: ValidatorFn = (grupo: AbstractControl): ValidationErrors | null => {
  const tipo1 = grupo.get('tipo1')?.value;
  const tipo2 = grupo.get('tipo2')?.value;
  return tipo2 && tipo2 === tipo1 ? { tiposRepetidos: true } : null;
};

/** Campo del formulario → campo que usa la API en sus errores de validación. */
const CAMPO_API: Record<string, string> = { tipo1: 'tipos', tipo2: 'tipos' };

/**
 * Registrar (/admin/pokemon/nuevo) o editar (/admin/pokemon/:numero) un Pokémon (HU-13).
 */
@Component({
  selector: 'app-formulario',
  templateUrl: 'formulario.page.html',
  styleUrls: ['admin.scss'],
  imports: [IonBackButton, IonButtons, IonContent, IonHeader, IonSpinner, IonTitle, IonToolbar, NumeroPokedexPipe, ReactiveFormsModule],
})
export class FormularioPage {
  private readonly pokemonService = inject(PokemonService);
  private readonly navegacion = inject(NavController);
  private readonly toasts = inject(ToastController);

  /** Parámetro :numero de la ruta; sin él, se registra un Pokémon nuevo. */
  readonly numero = input<string>();

  protected readonly editando = computed(() => this.numero() !== undefined);
  protected readonly generos = GENEROS;
  protected readonly estadisticas = ESTADISTICAS;
  protected readonly tipos = toSignal(this.pokemonService.listarTipos().pipe(catchError(() => of([]))), {
    initialValue: [],
  });

  protected readonly cargando = signal(false);
  protected readonly noEncontrado = signal(false);
  protected readonly nombreOriginal = signal('');
  protected readonly enviando = signal(false);
  /** true después del primer intento de guardar: muestra todos los errores. */
  protected readonly intentoGuardar = signal(false);
  protected readonly errorGeneral = signal<string | null>(null);
  protected readonly erroresApi = signal<Record<string, string>>({});

  private readonly fb = inject(FormBuilder);
  protected readonly formulario = this.fb.group(
    {
      numero: this.fb.control<number | null>(null, [Validators.required, Validators.min(1), Validators.max(9999)]),
      nombre: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(50)]),
      descripcion: this.fb.nonNullable.control('', Validators.maxLength(1000)),
      imagenUrl: this.fb.nonNullable.control('', [Validators.maxLength(300), Validators.pattern(/^$|^https?:\/\/\S+$/)]),
      altura: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.1), Validators.max(100)]),
      peso: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.1), Validators.max(1000)]),
      genero: this.fb.nonNullable.control(GENEROS[0], Validators.required),
      categoria: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(80)]),
      habilidad: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(80)]),
      tipo1: this.fb.nonNullable.control('', Validators.required),
      tipo2: this.fb.nonNullable.control(''),
      estadisticas: this.fb.group({
        ps: this.estadistica(),
        ataque: this.estadistica(),
        defensa: this.estadistica(),
        ataqueEspecial: this.estadistica(),
        defensaEspecial: this.estadistica(),
        velocidad: this.estadistica(),
      }),
    },
    { validators: tiposDistintos },
  );

  constructor() {
    // Al editar se cargan los datos actuales; el número no se puede cambiar
    effect(() => {
      const numero = this.numero();
      if (numero === undefined) {
        return;
      }
      this.formulario.controls.numero.disable();
      this.cargando.set(true);
      this.pokemonService
        .obtenerPorNumero(Number(numero))
        .pipe(finalize(() => this.cargando.set(false)))
        .subscribe({
          next: (pokemon) => this.llenar(pokemon),
          error: () => this.noEncontrado.set(true),
        });
    });
  }

  /** Mensaje de error de un campo: primero el del navegador, después el que devolvió la API. */
  protected error(ruta: string): string | null {
    const control = this.formulario.get(ruta);
    if (control?.invalid && (control.touched || this.intentoGuardar())) {
      return mensajeValidacion(control.errors ?? {});
    }
    if (ruta === 'tipo2' && this.formulario.hasError('tiposRepetidos') && (control?.touched || this.intentoGuardar())) {
      return 'Los tipos no se pueden repetir';
    }
    return this.erroresApi()[CAMPO_API[ruta] ?? ruta] ?? null;
  }

  protected guardar(): void {
    this.intentoGuardar.set(true);
    this.errorGeneral.set(null);
    this.erroresApi.set({});
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      this.errorGeneral.set(MENSAJE_REVISAR_CAMPOS);
      return;
    }

    const datos = this.datosFormulario();
    const peticion = this.editando()
      ? this.pokemonService.editar(Number(this.numero()), datos)
      : this.pokemonService.registrar(datos);
    this.enviando.set(true);
    peticion.pipe(finalize(() => this.enviando.set(false))).subscribe({
      next: (pokemon) => void this.terminar(pokemon),
      error: (error: unknown) => this.mostrarErrorApi(error),
    });
  }

  private async terminar(pokemon: PokemonDetalle): Promise<void> {
    const texto = this.editando() ? `Se guardaron los cambios de ${pokemon.nombre}.` : `Se registró a ${pokemon.nombre}.`;
    const toast = await this.toasts.create({ message: texto, duration: 2500, position: 'bottom', color: 'success' });
    await toast.present();
    await this.navegacion.navigateBack('/admin');
  }

  private mostrarErrorApi(error: unknown): void {
    const cuerpo = error instanceof HttpErrorResponse ? (error.error as ErrorApi | null) : null;
    if (error instanceof HttpErrorResponse && error.status === 400 && cuerpo?.errores) {
      this.erroresApi.set(cuerpo.errores);
      this.errorGeneral.set(MENSAJE_REVISAR_CAMPOS);
    } else if (error instanceof HttpErrorResponse && (error.status === 409 || error.status === 404) && cuerpo?.mensaje) {
      this.errorGeneral.set(cuerpo.mensaje);
    } else if (error instanceof HttpErrorResponse && (error.status === 401 || error.status === 403)) {
      this.errorGeneral.set('Tu sesión de administrador terminó. Vuelve a iniciar sesión.');
    } else {
      this.errorGeneral.set('No se pudo conectar con la API de la Pokédex.');
    }
  }

  private datosFormulario(): PokemonFormulario {
    const v = this.formulario.getRawValue();
    const e = v.estadisticas;
    return {
      numero: v.numero,
      nombre: v.nombre.trim(),
      descripcion: v.descripcion.trim(),
      imagenUrl: v.imagenUrl.trim(),
      altura: v.altura!,
      peso: v.peso!,
      genero: v.genero,
      categoria: v.categoria.trim(),
      habilidad: v.habilidad.trim(),
      tipos: [v.tipo1, v.tipo2].filter((tipo) => tipo !== ''),
      estadisticas: {
        ps: e.ps!,
        ataque: e.ataque!,
        defensa: e.defensa!,
        ataqueEspecial: e.ataqueEspecial!,
        defensaEspecial: e.defensaEspecial!,
        velocidad: e.velocidad!,
      },
    };
  }

  private llenar(pokemon: PokemonDetalle): void {
    this.nombreOriginal.set(pokemon.nombre);
    this.formulario.patchValue({
      numero: pokemon.numero,
      nombre: pokemon.nombre,
      descripcion: pokemon.descripcion ?? '',
      // La imagen oficial del número se vuelve a poner sola si el campo queda vacío
      imagenUrl: pokemon.imagenUrl?.includes('/official-artwork/') ? '' : (pokemon.imagenUrl ?? ''),
      altura: pokemon.altura,
      peso: pokemon.peso,
      genero: pokemon.genero,
      categoria: pokemon.categoria,
      habilidad: pokemon.habilidad,
      tipo1: pokemon.tipos[0]?.clave ?? '',
      tipo2: pokemon.tipos[1]?.clave ?? '',
      estadisticas: pokemon.estadisticas,
    });
  }

  private estadistica() {
    return this.fb.control<number | null>(null, [Validators.required, Validators.min(1), Validators.max(255)]);
  }
}

/** Texto en español para el primer error de validación de un campo. */
function mensajeValidacion(errores: ValidationErrors): string {
  if (errores['required']) {
    return 'Este campo es obligatorio';
  }
  if (errores['min'] || errores['max']) {
    const { min } = errores['min'] ?? {};
    const { max } = errores['max'] ?? {};
    return min !== undefined ? `Debe ser mayor o igual a ${min}` : `Debe ser menor o igual a ${max}`;
  }
  if (errores['maxlength']) {
    return `Máximo ${errores['maxlength'].requiredLength} caracteres`;
  }
  if (errores['pattern']) {
    return 'Debe ser una URL que empiece por http:// o https://';
  }
  return 'Valor no válido';
}
