import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular';

import { environment } from '../../../environments/environment';
import { PokemonResumen, Tipo } from '../../models/pokemon.model';
import { ListadoPage } from './listado.page';

const LISTADO: PokemonResumen[] = [
  { numero: 1, nombre: 'Bulbasaur', imagenUrl: 'https://img/1.png', tipos: [{ clave: 'grass', nombre: 'Planta' }, { clave: 'poison', nombre: 'Veneno' }] },
  { numero: 4, nombre: 'Charmander', imagenUrl: 'https://img/4.png', tipos: [{ clave: 'fire', nombre: 'Fuego' }] },
];
const TIPOS: Tipo[] = [
  { clave: 'poison', nombre: 'Veneno' },
  { clave: 'fire', nombre: 'Fuego' },
  { clave: 'grass', nombre: 'Planta' },
];

describe('ListadoPage', () => {
  let fixture: ComponentFixture<ListadoPage>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListadoPage],
      providers: [provideIonicAngular(), provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    fixture = TestBed.createComponent(ListadoPage);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  async function cargar(): Promise<HTMLElement> {
    fixture.detectChanges();
    await fixture.whenStable();
    http.expectOne(`${environment.apiUrl}/tipos`).flush(TIPOS);
    http.expectOne(`${environment.apiUrl}/pokemon`).flush(LISTADO);
    fixture.detectChanges();
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('CA1: muestra cada Pokémon con número, nombre, imagen y tipos en orden', async () => {
    const html = await cargar();
    const tarjetas = Array.from(html.querySelectorAll('.tarjeta'));

    expect(tarjetas.length).toBe(2);
    expect(tarjetas[0].textContent).toContain('N.º 0001');
    expect(tarjetas[0].textContent).toContain('Bulbasaur');
    expect(tarjetas[0].textContent).toContain('Planta');
    expect(tarjetas[0].textContent).toContain('Veneno');
    expect(tarjetas[0].querySelector('img')?.getAttribute('src')).toBe('https://img/1.png');
    expect(tarjetas[1].textContent).toContain('Charmander');
  });

  it('CA2: cada tarjeta enlaza a la ficha del Pokémon', async () => {
    const html = await cargar();
    const enlaces = Array.from(html.querySelectorAll('a.tarjeta')).map((a) => a.getAttribute('href'));

    expect(enlaces).toEqual(['/pokemon/1', '/pokemon/4']);
  });

  describe('HU-03 búsqueda', () => {
    async function buscar(texto: string): Promise<void> {
      const buscador = fixture.nativeElement.querySelector('ion-searchbar') as HTMLElement;
      buscador.dispatchEvent(new CustomEvent('ionInput', { detail: { value: texto } }));
      fixture.detectChanges();
      await fixture.whenStable();
    }

    it('CA1: envía el texto a la API y muestra los que coinciden', async () => {
      await cargar();
      await buscar('0004');
      const peticion = http.expectOne((r) => r.url === `${environment.apiUrl}/pokemon` && r.params.get('q') === '0004');
      peticion.flush([LISTADO[1]]);
      fixture.detectChanges();
      await fixture.whenStable();

      const tarjetas = (fixture.nativeElement as HTMLElement).querySelectorAll('.tarjeta');
      expect(tarjetas.length).toBe(1);
      expect(tarjetas[0].textContent).toContain('Charmander');
    });

    it('CA2: sin coincidencias muestra «No se encontraron Pokémon»', async () => {
      await cargar();
      await buscar('xyz');
      http.expectOne((r) => r.params.get('q') === 'xyz').flush([]);
      fixture.detectChanges();
      await fixture.whenStable();

      const mensaje = (fixture.nativeElement as HTMLElement).querySelector('[data-prueba="sin-resultados"]');
      expect(mensaje?.textContent?.trim()).toBe('No se encontraron Pokémon');
    });
  });

  describe('HU-04 filtro por tipo', () => {
    async function pulsarTipo(clave: string): Promise<void> {
      const boton = (fixture.nativeElement as HTMLElement).querySelector(`[data-prueba="tipo-${clave}"]`) as HTMLButtonElement;
      boton.click();
      fixture.detectChanges();
      await fixture.whenStable();
    }

    async function responder(pokemon: PokemonResumen[], filtro: (r: { params: { get(n: string): string | null } }) => boolean): Promise<void> {
      http.expectOne((r) => r.url === `${environment.apiUrl}/pokemon` && filtro(r)).flush(pokemon);
      fixture.detectChanges();
      await fixture.whenStable();
    }

    function nombres(): string[] {
      return Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('.tarjeta .nombre')).map(
        (n) => n.textContent?.trim() ?? '',
      );
    }

    it('muestra «Todos» y un botón por cada tipo', async () => {
      const html = await cargar();
      const botones = Array.from(html.querySelectorAll('.filtro-tipos .chip')).map((b) => b.textContent?.trim());

      expect(botones).toEqual(['Todos', 'Veneno', 'Fuego', 'Planta']);
      expect(html.querySelector('[data-prueba="tipo-todos"]')?.getAttribute('aria-pressed')).toBe('true');
    });

    it('CA1: al elegir un tipo pide a la API solo los Pokémon de ese tipo', async () => {
      const html = await cargar();
      await pulsarTipo('fire');
      await responder([LISTADO[1]], (r) => r.params.get('tipo') === 'fire' && r.params.get('q') === null);

      expect(nombres()).toEqual(['Charmander']);
      expect(html.querySelector('[data-prueba="tipo-fire"]')?.getAttribute('aria-pressed')).toBe('true');
    });

    it('CA2: el tipo se combina con la búsqueda', async () => {
      await cargar();
      await pulsarTipo('poison');
      await responder([LISTADO[0]], (r) => r.params.get('tipo') === 'poison');

      const buscador = fixture.nativeElement.querySelector('ion-searchbar') as HTMLElement;
      buscador.dispatchEvent(new CustomEvent('ionInput', { detail: { value: 'char' } }));
      fixture.detectChanges();
      await fixture.whenStable();
      await responder([], (r) => r.params.get('tipo') === 'poison' && r.params.get('q') === 'char');

      const mensaje = (fixture.nativeElement as HTMLElement).querySelector('[data-prueba="sin-resultados"]');
      expect(mensaje?.textContent?.trim()).toBe('No se encontraron Pokémon');
    });

    it('CA3: «Todos» (o volver a pulsar el tipo elegido) quita el filtro', async () => {
      await cargar();
      await pulsarTipo('fire');
      await responder([LISTADO[1]], (r) => r.params.get('tipo') === 'fire');

      await pulsarTipo('todos');
      await responder(LISTADO, (r) => r.params.get('tipo') === null);
      expect(nombres()).toEqual(['Bulbasaur', 'Charmander']);

      await pulsarTipo('fire');
      await responder([LISTADO[1]], (r) => r.params.get('tipo') === 'fire');
      await pulsarTipo('fire');
      await responder(LISTADO, (r) => r.params.get('tipo') === null);
      expect(nombres()).toEqual(['Bulbasaur', 'Charmander']);
    });
  });
});
