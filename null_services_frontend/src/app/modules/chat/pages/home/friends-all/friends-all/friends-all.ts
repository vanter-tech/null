import { Component, OnInit, ChangeDetectorRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ConversationControllerService } from '../../../../../../services/api';

import { FriendsControllerService } from '../../../../../../services/api/api/friendsController.service';
import { FriendResponseDTO } from '../../../../../../services/api/model/friendResponseDTO';

@Component({
  selector: 'app-friends-all',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './friends-all.html',
  styleUrl: './friends-all.css',
})
export class FriendsAll implements OnInit {

  FriendsList: FriendResponseDTO[] = [];

  @Output() onOpenChat = new EventEmitter<{ conversationId: number, friendName: string }>();

  constructor
  (
    private friendsControllerService: FriendsControllerService,
    private cdr: ChangeDetectorRef,
    private conversationControllerService: ConversationControllerService

  ) {}

  ngOnInit(): void {
      this.fetchFriends();
  }

  fetchFriends(): void {

    this.friendsControllerService.getMyFriends().subscribe({
      next: (data) => {
        this.FriendsList = data;
        console.log('Friends loaded in Friends All Tab:', this.FriendsList);

        this.cdr.detectChanges();
        
      },
      error: (error) => {
        console.error('Error fetching friends:', error);
      }
    });

  }

   startChat(friendId: number | undefined, friendName: string | undefined): void{
    if (!friendId || !friendName) return;

    this.conversationControllerService.createConversation(friendId as any).subscribe({
      next: (response: any) => {
        this.onOpenChat.emit({ 
          conversationId: response.id, 
          friendName: friendName });
      },
      error: (error) => {
        console.error('Error creating conversation:', error);
      }
    });
    
   }
  



}
