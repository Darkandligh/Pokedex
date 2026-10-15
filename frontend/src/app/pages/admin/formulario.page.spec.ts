import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { NavController, ToastController, provideIonicAngular } from '@ionic/angular';

import { environment } from '../../../environments/environment';
import { PokemonDetalle, Tipo } from '../../models/pokemon.model';
import { FormularioPage, MENSAJE_REVISAR_CAMPOS } from './formulario.page';

const API = environment.apiUrl;
const TIPOS: Tipo[] = [
  { clave: 'flying', nombre: 'Volador' },
  { clave: 'fire', nombre: 'Fuego' },
  { clave: 'electric', nombre: 'Eléctrico' },
];

const CHARMANDER: PokemonDetalle = {
  numero: 4,
  nombre: 'Charmander',
  descripcion: 'Prefiere las cosas calientes.',
  imagenUrl: 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png',
  altura: 0.6,
  peso: 8.5,
  genero: 'Macho y hembra',
  categoria: 'Lagartija',
  habilidad: 'Mar Llamas',
  tipos: [{ clave: 'fire', nombre: 'Fuego' }],
  debilidades: [],
  estadisticas: { ps: 39, ataque: 52, defensa: 43, ataqueEspecial: 60, defensaEspecial: 50, velocidad: 65 },
  cadenaEvolutiva: [],
  anterior: 3,
  siguiente: 5,
};

describe('FormularioPage (HU-13)', () => {
  let fixture: ComponentFixture<FormularioPage>;
  let http: HttpTestingController;
  let html: HTMLElement;
  const navegacion = { navigateBack: vi.fn(() => Promise.resolve(true)) };
  const toasts = { create: vi.fn(() => Promise.resolve({ present: () => Promise.resolve() })) };

  async function abrir(numero?: string): Promise<void> {
    navegacion.navigateBack.mockClear();
    toasts.create.mockClear();
    await TestBed.configureTestingModule({
      imports: [FormularioPage],
      providers: [
        provideIonicAngular(),
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: NavController, useValue: navegacion },
        { provide: ToastController, useValue: toasts },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(FormularioPage);
    if (numero) {
      fixture.componentRef.setInput('numero', numero);
    }
    http = TestBed.inject(HttpTestingController);
    html = fixture.nativeElement as HTMLElement;
    await actualizar();
    http.expectOne(`${API}/tipos`).flush(TIPOS);
    if (numero) {
      http.expectOne(`${API}/pokemon/${numero}`).flush(CHARMANDER);
    }
    await actualizar();
  }

  afterEach(() => http.verify());

  async function actualizar(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
    await new Promise((resolver) => setTimeout(resolver));
    fixture.detectChanges();
  }

  function campo(prueba: string): HTMLInputElement {
    return html.querySelector(`[data-prueba="${prueba}"]`) as HTMLInputElement;
  }

  function escribir(prueba: string, valor: string): void {
    const elemento = campo(prueba);
    elemento.value = valor;
    elemento.dispatchEvent(new Event(elemento.tagName === 'SELECT' ? 'change' : 'input'));
  }

  function llenarPikachu(): void {
    escribir('numero', '25');
    escribir('nombre', 'Pikachu');
    escribir('categoria', 'Ratón');
    escribir('habilidad', 'Electricidad Estática');
    escribir('altura', '0.4');
    escribir('peso', '6');
    escribir('tipo1', 'electric');
    const valores = { ps: 35, ataque: 55, defensa: 40, ataqueEspecial: 50, defensaEspecial: 50, velocidad: 90 };
    for (const [clave, valor] of Object.entries(valores)) {
      escribir(`estadistica-${clave}`, String(valor));
    }
  }

  async function guardar(): Promise<void> {
    html.querySelector('form')!.dispatchEvent(new Event('submit'));
    await actualizar();
  }

  function texto(prueba: string): string | undefined {
    return html.querySelector(`[data-prueba="${prueba}"]`)?.textContent?.trim();
  }

  it('CA1: registra el Pokémon con los datos del formulario y vuelve al panel', async () => {
    await abrir();
    llenarPikachu();
    await guardar();

    const peticion = http.expectOne({ method: 'POST', url: `${API}/pokemon` });
    expect(peticion.request.body).toEqual({
      numero: 25,
      nombre: 'Pikachu',
      descripcion: '',
      imagenUrl: '',
      altura: 0.4,
      peso: 6,
      genero: 'Macho y hembra',
      categoria: 'Ratón',
      habilidad: 'Electricidad Estática',
      tipos: ['electric'],
      estadisticas: { ps: 35, ataque: 55, defensa: 40, ataqueEspecial: 50, defensaEspecial: 50, velocidad: 90 },
    });
    peticion.flush({ ...CHARMANDER, numero: 25, nombre: 'Pikachu' });
    await actualizar();

    expect(toasts.create).toHaveBeenCalledWith(expect.objectContaining({ message: 'Se registró a Pikachu.' }));
    expect(navegacion.navigateBack).toHaveBeenCalledWith('/admin');
  });

  it('CA2: al editar carga los datos actuales, no deja cambiar el número y guarda con PUT', async () => {
    await abrir('4');

    expect(campo('nombre').value).toBe('Charmander');
    expect(campo('numero').disabled).toBe(true);
    expect(campo('tipo1').value).toBe('fire');
    expect(campo('estadistica-velocidad').value).toBe('65');
    // La imagen oficial no se muestra: si el campo queda vacío, la API la vuelve a poner
    expect(campo('imagenUrl').value).toBe('');

    escribir('nombre', 'Charmander Azul');
    escribir('tipo2', 'flying');
    await guardar();

    const peticion = http.expectOne({ method: 'PUT', url: `${API}/pokemon/4` });
    expect(peticion.request.body).toEqual(
      expect.objectContaining({ numero: 4, nombre: 'Charmander Azul', tipos: ['fire', 'flying'] }),
    );
    peticion.flush({ ...CHARMANDER, nombre: 'Charmander Azul' });
    await actualizar();

    expect(toasts.create).toHaveBeenCalledWith(
      expect.objectContaining({ message: 'Se guardaron los cambios de Charmander Azul.' }),
    );
  });

  it('CA3: con campos vacíos o fuera de rango no envía nada y marca cada campo', async () => {
    await abrir();
    escribir('estadistica-ps', '300');
    escribir('imagenUrl', 'foto.png');
    escribir('tipo1', 'fire');
    escribir('tipo2', 'fire');
    await guardar();

    http.expectNone({ method: 'POST', url: `${API}/pokemon` });
    expect(texto('error-general')).toBe(MENSAJE_REVISAR_CAMPOS);
    expect(texto('error-nombre')).toBe('Este campo es obligatorio');
    expect(html.textContent).toContain('Debe ser menor o igual a 255');
    expect(html.textContent).toContain('Debe ser una URL que empiece por http:// o https://');
    expect(html.textContent).toContain('Los tipos no se pueden repetir');
  });

  it('CA3: muestra en cada campo los errores de validación que devuelve la API', async () => {
    await abrir();
    llenarPikachu();
    await guardar();

    http.expectOne({ method: 'POST', url: `${API}/pokemon` }).flush(
      { estado: 400, mensaje: 'Datos inválidos', errores: { nombre: 'El nombre puede tener máximo 50 caracteres', tipos: 'Tipo desconocido: luz' } },
      { status: 400, statusText: 'Bad Request' },
    );
    await actualizar();

    expect(texto('error-nombre')).toBe('El nombre puede tener máximo 50 caracteres');
    expect(texto('error-tipos')).toBe('Tipo desconocido: luz');
    expect(navegacion.navigateBack).not.toHaveBeenCalled();
  });

  it('CA3: si el número ya existe muestra el mensaje de la API', async () => {
    await abrir();
    llenarPikachu();
    await guardar();

    http.expectOne({ method: 'POST', url: `${API}/pokemon` }).flush(
      { estado: 409, mensaje: 'Ya existe un Pokémon con el N.º 0025' },
      { status: 409, statusText: 'Conflict' },
    );
    await actualizar();

    expect(texto('error-general')).toBe('Ya existe un Pokémon con el N.º 0025');
  });
});
