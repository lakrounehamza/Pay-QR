import { Routes } from '@angular/router';
import { EnterpriseLayout } from './layout/enterprise-layout';

export const ENTERPRISE_ROUTES: Routes = [
  {
    path: '',
    component: EnterpriseLayout,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./pages/dashboard/enterprise-dashboard').then(
            (m) => m.EnterpriseDashboard
          ),
      },
      {
        path: 'users',
        loadComponent: () =>
          import('./pages/users/enterprise-users').then(
            (m) => m.EnterpriseUsers
          ),
      },
      {
        path: 'funding',
        loadComponent: () =>
          import('./pages/funding/enterprise-funding').then(
            (m) => m.EnterpriseFunding
          ),
      },
      {
        path: 'wallet',
        loadComponent: () =>
          import('./pages/wallet/enterprise-wallet').then(
            (m) => m.EnterpriseWallet
          ),
      },
    ],
  },
];
