import { Component, OnInit, ChangeDetectorRef, Output, EventEmitter, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs'; // 🚀 Importante para limpiar memoria

import { ConversationControllerService } from '../../../../../../../services/api';
import { FriendResponseDTO } from '../../../../../../../services/api/model/friendResponseDTO';
import { FriendsDataService } from '../../../../../../../services/api/friends-data-service/friends-data-service'; // 🚀 IMPORTA TU NUEVO SERVICIO AQUÍ (Ajusta la ruta si es necesario)

@Component({
  selector: 'app-friends-all',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './friends-all.html',
  styleUrl: './friends-all.css',
})
export class FriendsAll implements OnInit, OnDestroy {

  FriendsList: FriendResponseDTO[] = [];
  private friendsSub?: Subscription; // Guardamos la suscripción

  @Output() onOpenChat = new EventEmitter<{ conversationId: number, friendName: string }>();

  constructor(
    private friendsDataService: FriendsDataService, // 🚀 Inyectamos el servicio compartido en lugar del API directa
    private cdr: ChangeDetectorRef,
    private conversationControllerService: ConversationControllerService
  ) {}

  ngOnInit(): void {
      this.fetchFriends();
  }

  ngOnDestroy(): void {
      // 🧹 Limpiamos la memoria cuando el usuario cambie de pestaña
      this.friendsSub?.unsubscribe();
  }

  fetchFriends(): void {
    // 1. Nos suscribimos a la "fuente de verdad" del servicio compartido
    this.friendsSub = this.friendsDataService.allFriends$.subscribe({
      next: (data) => {
        this.FriendsList = data;
        console.log('Friends loaded from Shared Service:', this.FriendsList);
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error fetching friends from service:', error);
      }
    });

    // 2. Le pedimos al servicio que cargue los amigos. 
    // Él sabrá si necesita llamar a la API o si ya los tiene en memoria.
    this.friendsDataService.loadAllFriends();
  }

  startChat(friendId: number | undefined, friendName: string | undefined): void {
    if (!friendId || !friendName) return;

    // Tu lógica de crear chat sigue intacta y perfecta
    this.conversationControllerService.createConversation(friendId as any).subscribe({
      next: (response: any) => {
        this.onOpenChat.emit({ 
          conversationId: response.id, 
          friendName: friendName 
        });
      },
      error: (error) => {
        console.error('Error creating conversation:', error);
      }
    });
  }

}