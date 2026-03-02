import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./layout/admin-layout').then(m => m.AdminLayout),
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./pages/dashboard/dashboard').then(m => m.AdminDashboard),
      },
      {
        path: 'users',
        loadComponent: () =>
          import('./pages/users/users').then(m => m.AdminUsers),
      },
      {
        path: 'accounts',
        loadComponent: () =>
          import('./pages/accounts/accounts').then(m => m.AdminAccounts),
      },
      {
        path: 'enterprises',
        loadComponent: () =>
          import('./pages/enterprises/enterprises').then(m => m.AdminEnterprises),
      },
    ],
  },
];
