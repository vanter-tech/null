import { Component, OnInit, ChangeDetectorRef, Output, EventEmitter, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs'; 

import { FriendsControllerService } from '../../../../../../../services/api';
import { ConversationControllerService } from '../../../../../../../services/api';
import { FriendResponseDTO } from '../../../../../../../services/api/model/friendResponseDTO';
import { ConversationResponse } from '../../../../../../../services/api';
import { Token } from '../../../../../../../services/api/token/token';

import { PresenceService } from '../../../../../../../services/api/presence/presence';
import { Websocket } from '../../../../../../../services/api/websocket/websocket';
@Component({
  selector: 'app-friends-all',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './friends-all.html',
  styleUrl: './friends-all.css',
})
export class FriendsAll implements OnInit, OnDestroy {
  isLoading = true;
  FriendsList: FriendResponseDTO[] = [];
  private friendsSub?: Subscription; 
  myUserId!: number; 
  private subscriptions: Subscription = new Subscription();

  @Output() toOpenChat = new EventEmitter<{ conversationId: number | undefined, friendName: string }>();

  private friendService = inject(FriendsControllerService)
  private cdr = inject(ChangeDetectorRef)
  private conversationControllerService = inject(ConversationControllerService)
  private tokenService = inject(Token)
  private ws = inject(Websocket)
  private presenceService = inject(PresenceService)

  constructor() { this.extractIdFromToken(); }

  extractIdFromToken() {
    const tokenStr = this.tokenService.token;
    if(tokenStr){
      try{
        const payload = JSON.parse(atob(tokenStr.split('.')[1]));
        this.myUserId = payload.userId;
      }catch(e){
        console.error('Error al decodificar token', e);
      }
    }
  }

  ngOnInit(): void {
      this.ws.conectar();
      this.fetchFriends();
  }

  ngOnDestroy(): void {
      this.friendsSub?.unsubscribe();
  }

  fetchFriends(): void {
    this.isLoading = true;
    
    // 🛡️ IMPORTANTE: Si recargamos la lista, limpiamos las suscripciones viejas
    // para que no se dupliquen los eventos del WebSocket.
    this.subscriptions.unsubscribe();
    this.subscriptions = new Subscription();

    this.friendService.getMyFriends().subscribe({
      next: (friends) => {
        // 1. Filtrado inicial: Solo los que NO están OFFLINE
        this.FriendsList = friends
        this.isLoading = false;
        
        // 2. Suscribirse a cambios de estado para cada amigo cargado
        this.FriendsList.forEach(friend => {
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

  startChat(friendId: number | undefined, friendName: string | undefined): void {
    if (!friendId || !friendName) return;

    this.conversationControllerService.createConversation(friendId).subscribe({
      next: (response: ConversationResponse) => {
        this.toOpenChat.emit({ conversationId: response.id, friendName: friendName });
      },
      error: (error) => {
        console.error('Error creating conversation:', error);
      }
    });
  }

  private handleStatusUpdate(friendId: number, rawStatus: string): void {
    // 🚀 EL TRUCO DE MAGIA: Limpiamos las comillas y espacios basura que manda Spring Boot
    const newStatus = rawStatus.replace(/['"]+/g, '').trim(); 
    
    console.log(`Estado procesado para ID ${friendId}: [${newStatus}]`);
      const index = this.FriendsList.findIndex(f => f.id === friendId);
      if (index !== -1) {
        // Reemplazamos el objeto completo para disparar la detección de cambios
        this.FriendsList[index] = { ...this.FriendsList[index], status: newStatus as FriendResponseDTO.StatusEnum };
        this.cdr.detectChanges();
      } else if (newStatus !== 'OFFLINE') {
        // Si un amigo se conecta y no estaba en la lista, recargamos
        this.fetchFriends();
        this.cdr.detectChanges();
      }
    
  }
}