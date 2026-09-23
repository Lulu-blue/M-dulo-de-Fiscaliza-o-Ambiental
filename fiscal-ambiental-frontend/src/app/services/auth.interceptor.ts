import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();
  const isLoginRequest = req.url.includes('/api/auth/login');

  const finalReq = (token && !isLoginRequest)
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(finalReq).pipe(
    catchError((err) => {
      if (err.status === 401 && !isLoginRequest) {
        authService.logout();
      }
      return throwError(() => err);
    })
  );
};
