import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TokenService } from '../services/token.service';
import { UserRole } from '../models/auth.models';

export const authGuard: CanActivateFn = () => {
  const tokenSvc = inject(TokenService);
  const router   = inject(Router);
  if (tokenSvc.isLoggedIn()) return true;
  return router.createUrlTree(['/auth/login']);
};

export function roleGuard(...allowed: UserRole[]): CanActivateFn {
  return () => {
    const tokenSvc = inject(TokenService);
    const router   = inject(Router);
    const role     = tokenSvc.role();
    if (role && (allowed as string[]).includes(role)) return true;
    if (tokenSvc.isLoggedIn()) return router.createUrlTree(['/']);
    return router.createUrlTree(['/auth/login']);
  };
}
