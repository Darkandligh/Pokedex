import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import localeEs from '@angular/common/locales/es';
import { LOCALE_ID } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular';

import { environment } from '../../../environments/environment';
import { Noticia } from '../../models/pokemon.model';
import { MENSAJE_SIN_NOTICIAS, PortadaPage } from './portada.page';

const NOTICIAS: Noticia[] = [
  { id: 6, titulo: 'Filtra la Pokédex por tipo', resumen: 'Elige un tipo.', imagenUrl: 'https://img/6.png', fecha: '2026-10-15' },
  { id: 1, titulo: 'Arranca el proyecto Pokédex', resumen: 'Primer sprint.', imagenUrl: null, fecha: '2026-09-24' },
];

describe('PortadaPage (HU-06)', () => {
  let fixture: ComponentFixture<PortadaPage>;
  let http: HttpTestingController;

  beforeEach(async () => {
    registerLocaleData(localeEs);
    await TestBed.configureTestingModule({
      imports: [PortadaPage],
      providers: [
        provideIonicAngular(),
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: LOCALE_ID, useValue: 'es' },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(PortadaPage);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  async function cargar(noticias: Noticia[]): Promise<HTMLElement> {
    fixture.detectChanges();
    await fixture.whenStable();
    const peticion = http.expectOne((r) => r.url === `${environment.apiUrl}/noticias`);
    expect(peticion.request.params.get('limite')).toBe('6');
    peticion.flush(noticias);
    fixture.detectChanges();
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('CA1: muestra las noticias en el orden de la API, con fecha, título, resumen e imagen', async () => {
    const html = await cargar(NOTICIAS);
    const tarjetas = Array.from(html.querySelectorAll('[data-prueba="noticia"]'));

    expect(tarjetas.length).toBe(2);
    expect(tarjetas[0].querySelector('h3')?.textContent).toBe('Filtra la Pokédex por tipo');
    expect(tarjetas[0].querySelector('time')?.textContent).toBe('15 de octubre de 2026');
    expect(tarjetas[0].textContent).toContain('Elige un tipo.');
    expect(tarjetas[0].querySelector('img')?.getAttribute('src')).toBe('https://img/6.png');
    // Una noticia sin imagen se muestra igual, solo con el texto
    expect(tarjetas[1].querySelector('img')).toBeNull();
    expect(tarjetas[1].querySelector('time')?.textContent).toBe('24 de septiembre de 2026');
  });

  it('CA2: tiene un acceso a la Pokédex', async () => {
    const html = await cargar(NOTICIAS);

    expect(html.querySelector('[data-prueba="ir-pokedex"]')?.getAttribute('href')).toBe('/pokedex');
  });

  it('CA3: sin noticias muestra «No hay noticias por ahora» y sigue el acceso a la Pokédex', async () => {
    const html = await cargar([]);

    expect(html.querySelector('[data-prueba="sin-noticias"]')?.textContent?.trim()).toBe(MENSAJE_SIN_NOTICIAS);
    expect(html.querySelector('[data-prueba="ir-pokedex"]')).not.toBeNull();
  });

  it('si la API falla avisa sin bloquear el acceso a la Pokédex', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    http.expectOne((r) => r.url === `${environment.apiUrl}/noticias`).flush(null, { status: 0, statusText: 'Error' });
    fixture.detectChanges();
    await fixture.whenStable();
    const html = fixture.nativeElement as HTMLElement;

    expect(html.textContent).toContain('No se pudieron cargar las noticias.');
    expect(html.querySelector('[data-prueba="ir-pokedex"]')).not.toBeNull();
  });
});
