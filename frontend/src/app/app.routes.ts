import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'pokemon/:id',
    loadComponent: () => import('./pages/ficha/ficha.page').then((m) => m.FichaPage),
  },
  { path: '', redirectTo: 'pokemon/1', pathMatch: 'full' },
];
