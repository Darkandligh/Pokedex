import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'pokedex',
    loadComponent: () => import('./pages/listado/listado.page').then((m) => m.ListadoPage),
  },
  {
    path: 'pokemon/:id',
    loadComponent: () => import('./pages/ficha/ficha.page').then((m) => m.FichaPage),
  },
  { path: '', redirectTo: 'pokedex', pathMatch: 'full' },
  { path: '**', redirectTo: 'pokedex' },
];
