import { Routes } from '@angular/router';

import { adminGuard } from './services/admin.guard';

export const routes: Routes = [
  {
    path: 'pokedex',
    loadComponent: () => import('./pages/listado/listado.page').then((m) => m.ListadoPage),
  },
  {
    path: 'pokemon/:id',
    loadComponent: () => import('./pages/ficha/ficha.page').then((m) => m.FichaPage),
  },
  // HU-13: administración (registrar, editar y eliminar Pokémon)
  {
    path: 'admin/ingresar',
    loadComponent: () => import('./pages/admin/ingresar.page').then((m) => m.IngresarPage),
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin/panel.page').then((m) => m.PanelPage),
  },
  {
    path: 'admin/pokemon/nuevo',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin/formulario.page').then((m) => m.FormularioPage),
  },
  {
    path: 'admin/pokemon/:numero',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin/formulario.page').then((m) => m.FormularioPage),
  },
  { path: '', redirectTo: 'pokedex', pathMatch: 'full' },
  { path: '**', redirectTo: 'pokedex' },
];
