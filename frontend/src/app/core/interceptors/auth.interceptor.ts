// frontend/src/app/core/interceptors/auth.interceptor.ts
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('ec_token');
  const router = inject(Router);

  // Clone la requête pour y glisser le token s'il existe
  let authReq = req;
  if (token) {
    authReq = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  // Intercepte la réponse pour gérer les erreurs d'authentification
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Token expiré ou invalide : on nettoie et on redirige
        localStorage.removeItem('ec_token');
        localStorage.removeItem('ec_user');
        router.navigate(['/auth/login']);
      }
      return throwError(() => error);
    })
  );
};