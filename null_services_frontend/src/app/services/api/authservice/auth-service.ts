import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private initialName = localStorage.getItem('nickname') || 'Usuario'; 
  private nicknameSubject = new BehaviorSubject<string>(this.initialName);
  public nickname$ = this.nicknameSubject.asObservable();

  constructor() {

  }

  setNickname(nickname: string) {
    localStorage.setItem('nickname', nickname);
    this.nicknameSubject.next(nickname);
  }

  clearSesion(){
    localStorage.removeItem('token');
    localStorage.removeItem('nickname');
    this.nicknameSubject.next('Cargando...');
  }

}
