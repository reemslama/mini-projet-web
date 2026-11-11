import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  if (auth.authenticated()) return true;
  auth.login();
  return false;
};

export function roleGuard(...roles: string[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (!auth.authenticated()) {
      auth.login();
      return false;
    }
    if (auth.hasRole(...roles)) return true;
    return router.createUrlTree(['/'], {
      queryParams: { denied: '1' }
    });
  };
}
