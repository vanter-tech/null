import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

import { Modalservice } from '../../../../../../services/api/modalservice/modalservice';
// Asegúrate de que estas rutas apunten correctamente a tu API
import { AuthService } from '../../../../../../services/api/authservice/auth-service';
import { AuthenticationResponse } from '../../../../../../services/api';

/**
 * Componente responsable de renderizar y gestionar el modal de configuración de usuario.
 * Actúa como un observador pasivo del estado de autenticación (Single Source of Truth),
 * mostrando la información en tiempo real sin mutar los datos directamente.
 */
@Component({
  selector: 'app-user-settings-modal',
  standalone: true, 
  imports: [CommonModule],
  templateUrl: './user-settings-modal.html',
  styleUrl: './user-settings-modal.css',
})
export class UserSettingsModal implements OnInit, OnDestroy {

  /**
   * Almacena el estado actual del usuario autenticado.
   * Se actualiza automáticamente a través de la suscripción al AuthService.
   * @type {AuthenticationResponse | null}
   */
  currentUser: AuthenticationResponse | null = null;

  /**
   * Referencia a la suscripción reactiva del observable del usuario.
   * Necesaria para evitar fugas de memoria (memory leaks) al destruir el modal.
   * @private
   */
  private sub!: Subscription;

  /**
   * Inyecta las dependencias necesarias para el modal.
   * * @param modalService - Servicio para gestionar el estado de visibilidad del modal.
   * @param authService - Servicio centralizado de autenticación.
   */

  private modalService = inject(Modalservice)
  private authService = inject(AuthService)

  /**
   * Ciclo de vida de inicialización de Angular.
   * Establece la suscripción al flujo de datos del usuario (`currentUser$`) 
   * para mantener la interfaz sincronizada instantáneamente ante cualquier cambio.
   */
  ngOnInit(): void {
    this.sub = this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });
  }

  /**
   * Ciclo de vida de destrucción de Angular.
   * Asegura la cancelación de la suscripción reactiva para liberar recursos
   * del navegador cuando el modal se cierra.
   */
  ngOnDestroy(): void {
    if (this.sub) {
      this.sub.unsubscribe();
    }
  }

  /**
   * Delega la lógica de cierre del modal al servicio global de interfaz.
   * Se ejecuta, por ejemplo, al presionar la tecla ESC o hacer clic en la X.
   */
  closeModal(): void {
    this.modalService.closeSettings();
  }

}