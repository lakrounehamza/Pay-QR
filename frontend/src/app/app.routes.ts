import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'admin',
    canActivate: [roleGuard('ADMIN')],
    loadChildren: () =>
      import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES),
  },
  {
    path: 'enterprise',
    canActivate: [roleGuard('ENTERPRISE_ADMIN')],
    loadChildren: () =>
      import('./features/enterprise/enterprise.routes').then(m => m.ENTERPRISE_ROUTES),
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/user/home/pages/home-page/home-page').then(m => m.HomePage),
  },
  {
    path: 'wallet',
    canActivate: [roleGuard('USER')],
    loadComponent: () => import('./features/user/wallet/wallet').then(m => m.Wallet),
  },
  {
    path: 'welcome',
    loadComponent: () => import('./features/auth/pages/welcome/welcome').then(m => m.Welcome),
  },
  {
    path: 'otp',
    loadComponent: () => import('./layout/otp/otp').then(m => m.Otp),
  },
  {
    path: 'managed-qr',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/user/payqr/components/managed-qr/managed-qr').then(m => m.ManagedQR),
  },
  { path: '**', redirectTo: '/auth/login' },
];
