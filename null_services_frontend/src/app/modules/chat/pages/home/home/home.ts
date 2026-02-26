import { Component } from '@angular/core';
import { DmSidebar } from "../../../components/dm-sidebar/dm-sidebar";
import { CommonModule } from '@angular/common';

import { FriendsOnline } from '../friends-online/friends-online/friends-online';
import { FriendsAll } from '../friends-all/friends-all/friends-all';
import { FriendsPending } from '../friends-pending/friends-pending/friends-pending';
import { FriendAdd } from '../friends-add/friend-add/friend-add';

import { ChatRoom } from '../chat-room/chat-room/chat-room';

type tabType = 'ONLINE' | 'ALL' | 'PENDING' | 'ADD';
type ViewType = 'FRIENDS' | 'CHAT';

@Component({
  selector: 'app-home',
  imports: [DmSidebar, 
    CommonModule, 
    FriendsOnline, 
    FriendsAll, 
    FriendsPending,
    FriendAdd,
    ChatRoom],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home  {

  currentView: ViewType = 'FRIENDS';
  activeTab: tabType = 'ALL';

  // 1. Necesitamos guardar ambos datos para pasárselos al ChatRoom
  activeConversationId: number | null = null;
  activeFriendName: string = ''; // <--- Agregamos esta variable

  setTab(tab: tabType) {
    this.activeTab = tab;
  }

  // 2. Actualizamos el método para recibir el objeto del evento
  openChat(event: { conversationId: number, friendName: string }) {
    this.activeConversationId = event.conversationId;
    this.activeFriendName = event.friendName;
    this.currentView = 'CHAT';
  }

  openFriendsList() {
    this.currentView = 'FRIENDS';
    this.activeConversationId = null;
    this.activeFriendName = '';
  }

}
