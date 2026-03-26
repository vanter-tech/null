import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Token } from '../../api/token/token'; // Tu servicio de token

export const authGuard: CanActivateFn = () => {
  const tokenService = inject(Token); 
  const router = inject(Router);      
  
  if (tokenService.token) {
    return true; 
  } else {
    router.navigate(['/login']); 
    return false;
  }
};