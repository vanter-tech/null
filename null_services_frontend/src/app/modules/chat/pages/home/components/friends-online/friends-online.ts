import { Component, OnInit, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FriendsControllerService, FriendResponseDTO } from '../../../../../../services/api';
import { PresenceService } from '../../../../../../services/api/presence/presence';
import { Subscription } from 'rxjs';
import { Websocket } from '../../../../../../services/api/websocket/websocket';

@Component({
  selector: 'app-friends-online',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './friends-online.html',
  styleUrl: './friends-online.css',
})
export class FriendsOnline implements OnInit, OnDestroy {
  onlineFriends: Array<FriendResponseDTO> = [];
  isLoading: boolean = true;
  private subscriptions: Subscription = new Subscription();

  constructor(
    private friendService: FriendsControllerService,
    private cdr: ChangeDetectorRef,
    private presenceService: PresenceService,
    private ws: Websocket
  ) {}

  ngOnInit(): void {
    // 🚀 Se ejecuta automáticamente al entrar a la pestaña
    this.ws.conectar();
    this.loadFriends();
  }

  ngOnDestroy(): void {
    // Limpiamos todas las suscripciones de WebSocket al salir
    this.subscriptions.unsubscribe();
  }

  loadFriends(): void {
    this.isLoading = true;
    
    // 🛡️ IMPORTANTE: Si recargamos la lista, limpiamos las suscripciones viejas
    // para que no se dupliquen los eventos del WebSocket.
    this.subscriptions.unsubscribe();
    this.subscriptions = new Subscription();

    this.friendService.getMyFriends().subscribe({
      next: (friends) => {
        // 1. Filtrado inicial: Solo los que NO están OFFLINE
        this.onlineFriends = friends.filter(f => f && f.status && f.status !== 'OFFLINE');
        this.isLoading = false;
        
        // 2. Suscribirse a cambios de estado para cada amigo cargado
        this.onlineFriends.forEach(friend => {
          if (friend.id) {
            const statusSub = this.presenceService.watchUserStatus(friend.id).subscribe({
              next: (rawStatus) => {
                this.handleStatusUpdate(friend.id!, rawStatus);
              }
            });
            this.subscriptions.add(statusSub);
          }
        });
        this.cdr.detectChanges();
      },
      error: () => (this.isLoading = false)
    });
  }

  private handleStatusUpdate(friendId: number, rawStatus: string): void {
    // 🚀 EL TRUCO DE MAGIA: Limpiamos las comillas y espacios basura que manda Spring Boot
    const newStatus = rawStatus.replace(/['"]+/g, '').trim(); 
    
    console.log(`Estado procesado para ID ${friendId}: [${newStatus}]`);

    if (newStatus === 'OFFLINE') {
      // 1. Lo eliminamos del array
      this.onlineFriends = this.onlineFriends.filter(f => f.id !== friendId);
      // 2. Forzamos a Angular a re-renderizar la lista
      this.cdr.markForCheck(); 
      this.cdr.detectChanges();
    } else {
      const index = this.onlineFriends.findIndex(f => f.id === friendId);
      if (index !== -1) {
        // Reemplazamos el objeto completo para disparar la detección de cambios
        this.onlineFriends[index] = { ...this.onlineFriends[index], status: newStatus as any };
        this.cdr.detectChanges();
      } else if (newStatus !== 'OFFLINE') {
        // Si un amigo se conecta y no estaba en la lista, recargamos
        this.loadFriends();
      }
    }
  }
}