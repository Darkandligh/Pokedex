import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular';

import { environment } from '../../../environments/environment';
import { PokemonDetalle } from '../../models/pokemon.model';
import { FichaPage } from './ficha.page';

const IVYSAUR: PokemonDetalle = {
  numero: 2,
  nombre: 'Ivysaur',
  descripcion: 'Cuando le crece bastante el bulbo del lomo...',
  imagenUrl: 'https://img/2.png',
  altura: 1,
  peso: 13,
  genero: 'Macho y hembra',
  categoria: 'Semilla',
  habilidad: 'Espesura',
  tipos: [{ clave: 'grass', nombre: 'Planta' }, { clave: 'poison', nombre: 'Veneno' }],
  debilidades: [{ clave: 'fire', nombre: 'Fuego' }],
  estadisticas: { ps: 60, ataque: 62, defensa: 63, ataqueEspecial: 80, defensaEspecial: 80, velocidad: 60 },
  cadenaEvolutiva: [
    { numero: 1, nombre: 'Bulbasaur', imagenUrl: 'https://img/1.png', etapa: 0, disponible: true },
    { numero: 2, nombre: 'Ivysaur', imagenUrl: 'https://img/2.png', etapa: 1, disponible: true },
    { numero: 3, nombre: 'Venusaur', imagenUrl: 'https://img/3.png', etapa: 2, disponible: false },
  ],
  anterior: 1,
  siguiente: 3,
};

describe('FichaPage', () => {
  let fixture: ComponentFixture<FichaPage>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FichaPage],
      providers: [provideIonicAngular(), provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    fixture = TestBed.createComponent(FichaPage);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  async function abrir(id: string): Promise<HTMLElement> {
    fixture.componentRef.setInput('id', id);
    fixture.detectChanges();
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('CA1–CA3: muestra los datos, estadísticas y la cadena evolutiva con enlaces', async () => {
    await abrir('0002');
    http.expectOne(`${environment.apiUrl}/pokemon/2`).flush(IVYSAUR);
    fixture.detectChanges();
    await fixture.whenStable();
    const html = fixture.nativeElement as HTMLElement;

    expect(html.textContent).toContain('N.º 0002');
    expect(html.textContent).toContain('Semilla');
    expect(html.querySelectorAll('.estadisticas li').length).toBe(6);
    const enlaces = Array.from(html.querySelectorAll<HTMLAnchorElement>('a.eslabon')).map((a) => a.getAttribute('href'));
    expect(enlaces).toEqual(['/pokemon/1', '/pokemon/2']);
    // Venusaur no está disponible: se muestra sin enlace
    expect(html.querySelector('.eslabon.no-disponible')?.textContent).toContain('Venusaur');
  });

  it('HU-05 CA1: en el N.º 0002, «anterior» lleva al 0001 y «siguiente» al 0003', async () => {
    await abrir('2');
    http.expectOne(`${environment.apiUrl}/pokemon/2`).flush(IVYSAUR);
    fixture.detectChanges();
    await fixture.whenStable();
    const html = fixture.nativeElement as HTMLElement;

    const anterior = html.querySelector('[data-prueba="anterior"]');
    const siguiente = html.querySelector('[data-prueba="siguiente"]');
    expect(anterior?.getAttribute('href')).toBe('/pokemon/1');
    expect(anterior?.textContent).toContain('N.º 0001');
    expect(siguiente?.getAttribute('href')).toBe('/pokemon/3');
    expect(siguiente?.textContent).toContain('N.º 0003');
  });

  it('HU-05: en el primero de la Pokédex no aparece «anterior»', async () => {
    await abrir('1');
    http.expectOne(`${environment.apiUrl}/pokemon/1`).flush({ ...IVYSAUR, numero: 1, anterior: null, siguiente: 2 });
    fixture.detectChanges();
    await fixture.whenStable();
    const html = fixture.nativeElement as HTMLElement;

    expect(html.querySelector('[data-prueba="anterior"]')).toBeNull();
    expect(html.querySelector('[data-prueba="siguiente"]')?.getAttribute('href')).toBe('/pokemon/2');
  });

  it('CA4: si la API responde 404 muestra «Pokémon no encontrado»', async () => {
    await abrir('9999');
    http
      .expectOne(`${environment.apiUrl}/pokemon/9999`)
      .flush({ estado: 404, mensaje: 'Pokémon no encontrado' }, { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();
    await fixture.whenStable();

    const mensaje = (fixture.nativeElement as HTMLElement).querySelector('[data-prueba="no-encontrado"]');
    expect(mensaje?.textContent?.trim()).toBe('Pokémon no encontrado');
  });

  it('busca por nombre cuando el parámetro no es numérico', async () => {
    await abrir('missingno');
    http
      .expectOne(`${environment.apiUrl}/pokemon/nombre/missingno`)
      .flush(null, { status: 404, statusText: 'Not Found' });
  });
});
