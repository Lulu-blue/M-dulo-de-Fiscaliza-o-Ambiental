import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  const expectedRole = route.data?.['role'] as 'GESTOR' | 'FISCAL' | undefined;
  if (expectedRole) {
    const userRole = authService.currentUser()?.cargo;
    if (userRole !== expectedRole) {
      if (userRole === 'GESTOR') router.navigate(['/gestor']);
      else if (userRole === 'FISCAL') router.navigate(['/fiscal']);
      else router.navigate(['/login']);
      return false;
    }
  }

  return true;
};
