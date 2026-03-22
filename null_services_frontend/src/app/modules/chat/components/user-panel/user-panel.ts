import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { AuthService } from '../../../../services/api/authservice/auth-service';
import { Websocket } from '../../../../services/api/websocket/websocket';
import { AuthenticationResponse, UsersService } from '../../../../services/api';
import { Modalservice } from '../../../../services/api/modalservice/modalservice';
import { UserPanelPopUp } from './user-panel-pop-up/user-panel-pop-up/user-panel-pop-up';

@Component({
  selector: 'app-user-panel',
  standalone: true,
  imports: [CommonModule, UserPanelPopUp],
  templateUrl: './user-panel.html',
  styleUrl: './user-panel.css',
  host: {
    'class': 'contents'
  }
})
export class UserPanel implements OnInit, OnDestroy {

  username = 'Usuario';
  private sub!: Subscription;

  isStatusMenuOpen = false;
  currentStatus: AuthenticationResponse.StatusEnum = AuthenticationResponse.StatusEnum.Online;
  showSmallPanel = false;

  // 🚀 NUEVA VARIABLE: Guardamos el usuario completo
  currentUser: AuthenticationResponse | null = null;

  private router = inject(Router)
  private nickService = inject(AuthService)
  private userService = inject(UsersService)
  private ws = inject(Websocket)
  private modalService = inject(Modalservice)

  ngOnInit(): void {
    // 🚀 1. MAGIA REACTIVA: Nos suscribimos al objeto global
    this.sub = this.nickService.currentUser$.subscribe(user => {
      if (user) {
        this.currentUser = user;
        this.username = user.nickname || 'Usuario';
        this.currentStatus = user.status || AuthenticationResponse.StatusEnum.Online;
      }
    });
  }


  ngOnDestroy(): void {
    if (this.sub) {
      this.sub.unsubscribe();
    }
  }
  
  toggleStatusMenu(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.isStatusMenuOpen = !this.isStatusMenuOpen;
    this.showSmallPanel = this.isStatusMenuOpen; 
  }

  openSettingsModal(): void {
    if (this.showSmallPanel || this.isStatusMenuOpen) {
      return; 
    }
    this.modalService.openSettings();
  }

  onStatusChange(newStatus: AuthenticationResponse.StatusEnum): void {
    this.isStatusMenuOpen = false; 
    this.showSmallPanel = false;

    // Guardamos el estado anterior por si falla la llamada al backend
    const previousStatus = this.currentStatus;
    
    // 🚀 2. Actualizamos de forma reactiva a través del servicio central
    this.nickService.updateLocalStatus(newStatus);

    this.userService.updateStatus(newStatus).subscribe({
      next: () => console.log(`Estado guardado en BD: ${newStatus}`),
      error: (err) => {
        console.error('Error al guardar el estado', err);
        // 🚀 Si falla, revertimos usando el mismo servicio central
        this.nickService.updateLocalStatus(previousStatus);
      }
    });
  }

  logout(): void {
    console.log('Cerrando sesion...');

    if (this.ws.rxStomp.active) {
      this.ws.rxStomp.deactivate();
    }

    // 🚀 3. El servicio central se encarga de limpiar TODO (token, usuario, etc.)
    this.nickService.clearSesion();
    this.router.navigate(['/login']);
  }
}