import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AlertController, provideIonicAngular } from '@ionic/angular';

import { environment } from '../../../environments/environment';
import { PokemonResumen } from '../../models/pokemon.model';
import { PanelPage, ROL_CONFIRMAR } from './panel.page';

const API = environment.apiUrl;
const LISTADO: PokemonResumen[] = [
  { numero: 1, nombre: 'Bulbasaur', imagenUrl: 'https://img/1.png', tipos: [{ clave: 'grass', nombre: 'Planta' }] },
  { numero: 4, nombre: 'Charmander', imagenUrl: 'https://img/4.png', tipos: [{ clave: 'fire', nombre: 'Fuego' }] },
];

describe('PanelPage (HU-13)', () => {
  let fixture: ComponentFixture<PanelPage>;
  let http: HttpTestingController;
  let html: HTMLElement;
  let rolElegido: string;
  const alertas = {
    create: vi.fn((opciones: { message: string }) =>
      Promise.resolve({
        opciones,
        present: () => Promise.resolve(),
        onDidDismiss: () => Promise.resolve({ role: rolElegido }),
      }),
    ),
  };

  beforeEach(async () => {
    alertas.create.mockClear();
    await TestBed.configureTestingModule({
      imports: [PanelPage],
      providers: [
        provideIonicAngular(),
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AlertController, useValue: alertas },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(PanelPage);
    http = TestBed.inject(HttpTestingController);
    html = fixture.nativeElement as HTMLElement;
    fixture.componentInstance.ionViewWillEnter();
    http.expectOne(`${API}/pokemon`).flush(LISTADO);
    await actualizar();
  });

  afterEach(() => http.verify());

  async function actualizar(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
    await new Promise((resolver) => setTimeout(resolver));
    fixture.detectChanges();
  }

  async function pulsarEliminar(fila: number): Promise<void> {
    const botones = html.querySelectorAll<HTMLButtonElement>('[data-prueba="eliminar"]');
    botones[fila].click();
    await actualizar();
  }

  it('muestra cada Pokémon con sus botones de editar y eliminar', () => {
    const filas = html.querySelectorAll('[data-prueba="fila"]');

    expect(filas.length).toBe(2);
    expect(filas[0].textContent).toContain('Bulbasaur');
    expect(filas[0].querySelector('a')?.getAttribute('href')).toBe('/admin/pokemon/1');
    expect(html.querySelector('[data-prueba="registrar"]')?.getAttribute('href')).toBe('/admin/pokemon/nuevo');
  });

  it('CA4: pide confirmación y, si se confirma, elimina el Pokémon y recarga el listado', async () => {
    rolElegido = ROL_CONFIRMAR;
    await pulsarEliminar(1);

    expect(alertas.create).toHaveBeenCalledTimes(1);
    expect(alertas.create.mock.calls[0][0].message).toContain('¿Seguro que quieres eliminar a Charmander?');
    http.expectOne({ method: 'DELETE', url: `${API}/pokemon/4` }).flush(null, { status: 204, statusText: 'No Content' });
    http.expectOne(`${API}/pokemon`).flush([LISTADO[0]]);
    await actualizar();

    expect(html.querySelector('[data-prueba="aviso"]')?.textContent?.trim()).toBe('Se eliminó a Charmander.');
    expect(html.querySelectorAll('[data-prueba="fila"]').length).toBe(1);
  });

  it('CA4: si se cancela la confirmación no se elimina nada', async () => {
    rolElegido = 'cancel';
    await pulsarEliminar(0);

    expect(alertas.create).toHaveBeenCalledTimes(1);
    http.expectNone({ method: 'DELETE' });
    expect(html.querySelectorAll('[data-prueba="fila"]').length).toBe(2);
  });
});
