import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';

import { environment } from '../../environments/environment';
import { adminGuard } from './admin.guard';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

const API = environment.apiUrl;
const ENCABEZADO_ADMIN = `Basic ${btoa('admin:pokedex2026')}`;

describe('Sesión del administrador (HU-13)', () => {
  let auth: AuthService;
  let http: HttpTestingController;
  let cliente: HttpClient;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
    cliente = TestBed.inject(HttpClient);
  });

  afterEach(() => http.verify());

  function iniciarSesion(): void {
    auth.ingresar('admin', 'pokedex2026').subscribe();
    const peticion = http.expectOne(`${API}/admin/sesion`);
    expect(peticion.request.headers.get('Authorization')).toBe(ENCABEZADO_ADMIN);
    peticion.flush({ usuario: 'admin' });
  }

  it('con credenciales correctas guarda la sesión', () => {
    iniciarSesion();

    expect(auth.esAdmin()).toBe(true);
    expect(auth.usuario()).toBe('admin');
    expect(sessionStorage.getItem('pokedex.admin')).toContain('admin');
  });

  it('CA5: con credenciales incorrectas no se inicia sesión', () => {
    let fallo = false;
    auth.ingresar('admin', 'otra').subscribe({ error: () => (fallo = true) });
    http.expectOne(`${API}/admin/sesion`).flush({ estado: 401, mensaje: 'Debes iniciar sesión como administrador' }, { status: 401, statusText: 'Unauthorized' });

    expect(fallo).toBe(true);
    expect(auth.esAdmin()).toBe(false);
  });

  it('agrega las credenciales al registrar, editar y eliminar, pero no a las consultas', () => {
    iniciarSesion();

    cliente.get(`${API}/pokemon`).subscribe();
    cliente.post(`${API}/pokemon`, {}).subscribe();
    cliente.delete(`${API}/pokemon/4`).subscribe();

    expect(http.expectOne({ method: 'GET', url: `${API}/pokemon` }).request.headers.has('Authorization')).toBe(false);
    expect(http.expectOne({ method: 'POST', url: `${API}/pokemon` }).request.headers.get('Authorization')).toBe(ENCABEZADO_ADMIN);
    expect(http.expectOne({ method: 'DELETE', url: `${API}/pokemon/4` }).request.headers.get('Authorization')).toBe(ENCABEZADO_ADMIN);
  });

  it('si la API responde 401 la sesión se cierra', () => {
    iniciarSesion();

    cliente.delete(`${API}/pokemon/4`).subscribe({ error: () => undefined });
    http.expectOne(`${API}/pokemon/4`).flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(auth.esAdmin()).toBe(false);
  });

  describe('adminGuard', () => {
    const ruta = {} as ActivatedRouteSnapshot;
    const estado = { url: '/admin/pokemon/nuevo' } as RouterStateSnapshot;

    it('CA5: sin sesión lleva a iniciar sesión y recuerda la página pedida', () => {
      const resultado = TestBed.runInInjectionContext(() => adminGuard(ruta, estado));

      expect(resultado).toBeInstanceOf(UrlTree);
      expect((resultado as UrlTree).toString()).toBe('/admin/ingresar?volver=%2Fadmin%2Fpokemon%2Fnuevo');
    });

    it('con sesión deja entrar', () => {
      iniciarSesion();

      expect(TestBed.runInInjectionContext(() => adminGuard(ruta, estado))).toBe(true);
    });
  });
});
