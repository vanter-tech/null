import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { AuthenticationResponse } from '../model/authenticationResponse';
import { Token } from '../token/token'; // 🚀 Importa tu servicio de token

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private getInitialUser(): AuthenticationResponse | null {
    const storedUser = localStorage.getItem('currentUser');
    return storedUser ? JSON.parse(storedUser) : null;
  }

  private currentUserSubject = new BehaviorSubject<AuthenticationResponse | null>(this.getInitialUser());
  public currentUser$ = this.currentUserSubject.asObservable();
  public nickname$ = new BehaviorSubject<string>(this.getInitialUser()?.nickname || 'Usuario').asObservable();

  constructor(private tokenService: Token) {} // 🚀 Inyectamos el Token Service aquí

  // 🚀 NUEVO: Getter público para obtener el usuario actual sin suscribirse
  public get currentUserValue(): AuthenticationResponse | null {
    return this.currentUserSubject.value;
  }

  // 🚀 NUEVO: Método centralizado para obtener el ID ultra-seguro
  public getMyUserId(): number {
    // 1. Intentamos sacarlo del objeto guardado (lo más rápido)
    const user = this.currentUserValue;
    if (user && (user as any).id) {
      return (user as any).id;
    }

    // 2. Fallback: Si el objeto no tiene el ID, decodificamos el Token centralizadamente
    const tokenStr = this.tokenService.token;
    if (tokenStr) {
      try {
        const payload = JSON.parse(atob(tokenStr.split('.')[1]));
        return payload.userId || payload.id || 0; // Ajusta según el nombre en tu token
      } catch (e) {
        console.error('Error decodificando token en AuthService', e);
      }
    }
    return 0; // Si todo falla, devuelve 0
  }

  setCurrentUser(user: AuthenticationResponse) {
    localStorage.setItem('currentUser', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  updateLocalStatus(status: AuthenticationResponse.StatusEnum) {
    const current = this.currentUserSubject.value;
    if (current) {
      const updatedUser = { ...current, status: status };
      this.setCurrentUser(updatedUser);
    }
  }

  clearSesion() {
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }
}