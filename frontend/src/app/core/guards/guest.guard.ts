// frontend/src/app/core/guards/guest.guard.ts
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const guestGuard: CanActivateFn = () => {
  const token = localStorage.getItem('ec_token');
  if (!token) return true;
  
  inject(Router).navigate(['/events']);
  return false;
};