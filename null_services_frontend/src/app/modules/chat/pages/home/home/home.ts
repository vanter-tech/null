import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DmSidebar } from "../../../components/dm-sidebar/dm-sidebar";
import { FriendsOnline } from '../components/friends-online/friends-online';
import { FriendsAll } from '../components/friends-all/friends-all/friends-all';
import { FriendsPending } from '../components/friends-pending/friends-pending/friends-pending';
import { FriendAdd } from '../components/friends-add/friend-add/friend-add';
import { ChatRoom } from '../components/chat-room/chat-room/chat-room';

type tabType = 'ONLINE' | 'ALL' | 'PENDING' | 'ADD';
type ViewType = 'FRIENDS' | 'CHAT';

@Component({
  selector: 'app-home',
  imports: [
    CommonModule, 
    DmSidebar, 
    FriendsOnline, 
    FriendsAll, 
    FriendsPending,
    FriendAdd,
    ChatRoom
  ],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {

  currentView: ViewType = 'FRIENDS';
  activeTab: tabType = 'ALL';

  // Variables que alimentan el chat
  activeConversationId: number | null = null;
  activeFriendName: string = '';

  // 👇 El "Cerebro" que maneja los clics de cualquier parte 👇
  openChat(eventData: any) {
    console.log('Solicitud para abrir chat:', eventData);

    // CASO 1: Si el clic viene de la Barra Lateral (tiene 'otherUserName')
    if (eventData.otherUserName) {
      this.activeConversationId = eventData.id;
      this.activeFriendName = eventData.otherUserName;
    } 
    // CASO 2: Si el clic viene de tu Lista de Amigos (tiene 'friendName')
    else {
      this.activeConversationId = eventData.conversationId;
      this.activeFriendName = eventData.friendName;
    }

    // Finalmente, cambiamos la pantalla al modo CHAT
    this.currentView = 'CHAT';
  }

  setTab(tab: tabType) {
    this.activeTab = tab;
  }

  openFriendsList() {
    this.currentView = 'FRIENDS';
    this.activeConversationId = null;
    this.activeFriendName = '';
  }
}