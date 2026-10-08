import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular';

import { environment } from '../../../environments/environment';
import { PokemonResumen } from '../../models/pokemon.model';
import { ListadoPage } from './listado.page';

const LISTADO: PokemonResumen[] = [
  { numero: 1, nombre: 'Bulbasaur', imagenUrl: 'https://img/1.png', tipos: [{ clave: 'grass', nombre: 'Planta' }, { clave: 'poison', nombre: 'Veneno' }] },
  { numero: 4, nombre: 'Charmander', imagenUrl: 'https://img/4.png', tipos: [{ clave: 'fire', nombre: 'Fuego' }] },
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
});
