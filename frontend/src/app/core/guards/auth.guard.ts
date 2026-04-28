import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = () => {
  const token = localStorage.getItem('ec_token');
  if (token) return true;
  inject(Router).navigate(['/auth/login']);
  return false;
};