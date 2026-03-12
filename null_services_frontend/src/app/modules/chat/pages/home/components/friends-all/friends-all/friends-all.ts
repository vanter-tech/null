import { Component, OnInit, ChangeDetectorRef, Output, EventEmitter, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs'; 

import { ConversationControllerService } from '../../../../../../../services/api';
import { FriendResponseDTO } from '../../../../../../../services/api/model/friendResponseDTO';
import { FriendsDataService } from '../../../../../../../services/api/friends-data-service/friends-data-service'; 
import { Token } from '../../../../../../../services/api/token/token'; // 🚀 1. Importamos el Token

@Component({
  selector: 'app-friends-all',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './friends-all.html',
  styleUrl: './friends-all.css',
})
export class FriendsAll implements OnInit, OnDestroy {

  FriendsList: FriendResponseDTO[] = [];
  private friendsSub?: Subscription; 
  myUserId!: number; // 🚀 2. Variable para tu ID

  @Output() onOpenChat = new EventEmitter<{ conversationId: number, friendName: string }>();

  constructor(
    private friendsDataService: FriendsDataService, 
    private cdr: ChangeDetectorRef,
    private conversationControllerService: ConversationControllerService,
    private tokenService: Token // 🚀 3. Inyectamos el Token
  ) {
    this.extractIdFromToken(); // 🚀 4. Sacamos tu ID al iniciar
  }

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
      this.fetchFriends();
  }

  ngOnDestroy(): void {
      this.friendsSub?.unsubscribe();
  }

  fetchFriends(): void {
    this.friendsSub = this.friendsDataService.allFriends$.subscribe({
      next: (data) => {
        // 🚀 5. Filtramos la lista para que NO incluya tu propio ID
        this.FriendsList = data.filter(friend => friend.id !== this.myUserId);
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error fetching friends from service:', error);
      }
    });

    this.friendsDataService.loadAllFriends();
  }

  startChat(friendId: number | undefined, friendName: string | undefined): void {
    if (!friendId || !friendName) return;

    this.conversationControllerService.createConversation(friendId as any).subscribe({
      next: (response: any) => {
        this.onOpenChat.emit({ conversationId: response.id, friendName: friendName });
      },
      error: (error) => {
        console.error('Error creating conversation:', error);
      }
    });
  }
}