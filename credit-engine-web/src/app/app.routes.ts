import { Routes } from '@angular/router';
import { Shell } from './layout/shell/shell';

export const routes: Routes = [
  {
    path: '',
    component: Shell,
    children: [
      {
        path: '',
        redirectTo: 'simulation',
        pathMatch: 'full'
      },
      {
        path: 'simulation',
        loadComponent: () =>
          import('./pages/simulation/simulation').then((m) => m.Simulation)
      },
      {
        path: 'settlements',
        loadComponent: () =>
          import('./pages/settlements/settlements').then((m) => m.Settlements)
      }
    ]
  }
];
