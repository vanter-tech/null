import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core'; // 🚀 Importamos ChangeDetectorRef
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

import { DmSidebar } from "../../../components/dm-sidebar/dm-sidebar";
import { FriendsOnline } from '../components/friends-online/friends-online';
import { FriendsAll } from '../components/friends-all/friends-all/friends-all';
import { FriendsPending } from '../components/friends-pending/friends-pending/friends-pending';
import { FriendAdd } from '../components/friends-add/friend-add/friend-add';
import { ChatRoom } from '../components/chat-room/chat-room/chat-room';

import { Modalservice } from '../../../../../services/api/modalservice/modalservice';
// 🚀 IMPORTAMOS EL WALKIE-TALKIE
import { ChatNavigationService } from '../../../../../services/api/chat-navigation-service/chat-navigation-service';

type tabType = 'ONLINE' | 'ALL' | 'PENDING' | 'ADD';
type ViewType = 'FRIENDS' | 'CHAT';

@Component({
  selector: 'app-home',
  standalone: true, // 🚀 Añadí standalone por si acaso
  imports: [
    CommonModule, 
    DmSidebar, 
    FriendsOnline, 
    FriendsAll, 
    FriendsPending,
    FriendAdd,
    ChatRoom,
  ],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit, OnDestroy {

  currentView: ViewType = 'FRIENDS';
  activeTab: tabType = 'ALL';

  activeConversationId: number | null = null;
  activeFriendName: string = '';

  private chatNavSub?: Subscription;

  constructor(
    private chatNavigationService: ChatNavigationService,
    private modalService: Modalservice,
    private cdr: ChangeDetectorRef // 🚀 INYECTAMOS EL DETECTOR DE CAMBIOS
  ) {}

  ngOnInit() {
    // 📻 Nos ponemos a escuchar el Walkie-Talkie
    this.chatNavSub = this.chatNavigationService.openChat$.subscribe(data => {
      console.log('📻 home.ts escuchó el modal! Abriendo chat:', data);
      
      this.openChat(data);

      // 🚀 EL EMPUJÓN MÁGICO: Obligamos a Angular a mostrar el ChatRoom
      this.cdr.detectChanges(); 
    });
  }

  ngOnDestroy() {
    this.chatNavSub?.unsubscribe();
  }

  openChat(eventData: any) {
    console.log('Solicitud para abrir chat:', eventData);

    if (eventData.otherUserName) {
      this.activeConversationId = eventData.id;
      this.activeFriendName = eventData.otherUserName;
    } 
    else {
      this.activeConversationId = eventData.conversationId;
      this.activeFriendName = eventData.friendName;
    }

    // Cambiamos la vista
    this.currentView = 'CHAT';
  }

  setTab(tab: tabType) {
    this.activeTab = tab;
  }

  openCreateDMModal(): void{
    this.modalService.openCreateDm();
  }

  openFriendsList() {
    this.currentView = 'FRIENDS';
    this.activeConversationId = null;
    this.activeFriendName = '';
  }
}