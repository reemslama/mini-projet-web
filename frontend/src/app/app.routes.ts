import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/catalog/catalog').then((m) => m.CatalogPage)
  },
  {
    path: 'challenges/:id',
    loadComponent: () =>
      import('./pages/challenge-detail/challenge-detail').then((m) => m.ChallengeDetailPage)
  },
  {
    path: 'rankings',
    loadComponent: () => import('./pages/rankings/rankings').then((m) => m.RankingsPage)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/dashboard/dashboard').then((m) => m.DashboardPage)
  },
  {
    path: 'reputation',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/reputation/reputation').then((m) => m.ReputationPage)
  },
  {
    path: 'enterprise/new',
    canActivate: [roleGuard('ENTERPRISE', 'ADMIN')],
    loadComponent: () =>
      import('./pages/enterprise-new/enterprise-new').then((m) => m.EnterpriseNewPage)
  },
  {
    path: 'enterprise/challenges/:id',
    canActivate: [roleGuard('ENTERPRISE', 'ADMIN')],
    loadComponent: () =>
      import('./pages/enterprise-manage/enterprise-manage').then((m) => m.EnterpriseManagePage)
  },
  {
    path: 'evaluate',
    canActivate: [roleGuard('EXPERT', 'ADMIN')],
    loadComponent: () => import('./pages/evaluate/evaluate').then((m) => m.EvaluatePage)
  },
  { path: '**', redirectTo: '' }
];
