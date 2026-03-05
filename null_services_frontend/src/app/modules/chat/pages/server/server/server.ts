import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { ServerControllerService, ServerResponse, MessageControllerService, Message, AuthenticationService } from '../../../../../services/api';
import { Websocket } from '../../../../../services/api/websocket/websocket';
import { AuthService } from '../../../../../services/api/authservice/auth-service';
import { Token } from '../../../../../services/api/token/token';

import { CreateChannelModal } from '../../../components/modals/create-channel-modal/create-channel-modal/create-channel-modal';
import { DeleteServerModal } from '../../../components/modals/delete-server-modal/delete-server-modal/delete-server-modal';

/**
 * Componente principal para la gestión de servidores.
 * Controla la visualización de canales, lista de miembros y la lógica de chat en tiempo real.
 */
@Component({
  selector: 'app-server',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, CreateChannelModal, DeleteServerModal],
  templateUrl: './server.html',
  styleUrl: './server.css',
})
export class Server implements OnInit, OnDestroy {
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  isItMe(sendId: any): boolean {
    if (!sendId || !this.myUserId) return false;
    return Number(sendId) === Number(this.myUserId);
  }
  
  // Estado del Servidor
  currentServerId: number | null = null;
  serverData: ServerResponse | null = null;
  activeChannelId: number | null = null;
  
  // UI States
  isMembersListOpen: boolean = true;
  isServerMenuOpen: boolean = false;

  // Chat State
  messages: Message[] = [];
  chatInput: string = '';
  myUserId!: number;

  private topicSubscription?: Subscription;
  private routeSub?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private serverService: ServerControllerService,
    private messageService: MessageControllerService,
    private authService: AuthService,
    private ws: Websocket,
    private cdr: ChangeDetectorRef,
    private tokenService: Token
  ) {
    this.myUserId = this.authService.getMyUserId()
  }

  ngOnInit(): void {
    this.routeSub = this.route.paramMap.subscribe(param => {
      const idStr = param.get('serverId');
      if (idStr) {
        this.currentServerId = Number(idStr);
        this.loadServerData(this.currentServerId);
      }
    });
  }

  loadServerData(id: number): void {
    this.serverData = null;
    this.isServerMenuOpen = false;

    this.messages = [];
    this.activeChannelId = null;

    this.serverService.findServerById(id).subscribe({
      next: (data: ServerResponse) => {
        this.serverData = data;
        if (data.channels && data.channels.length > 0) {
          this.selectChannel(data.channels[0].id);
        } else {
          this.activeChannelId = null;
        }
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Failed to load server', err)
    });
  }

  selectChannel(channelId: number | undefined): void {
    if (channelId === undefined) return;
    
    this.activeChannelId = channelId;
    this.loadChannelHistory(channelId);
    this.connectToChannelTopic(channelId);
  }

  private loadChannelHistory(channelId: number): void {
    this.messageService.getChannelHistory(channelId as any).subscribe({
      next: (history) => {
        this.messages = history;
        this.cdr.detectChanges();
        setTimeout(() => this.scrollToBottom(), 50);
      },
      error: (err) => console.error('Error loading channel history', err)
    });
  }

  private connectToChannelTopic(channelId: number): void {
    if (this.topicSubscription) {
      this.topicSubscription.unsubscribe();
    }

    const topic = `/topic/channel.${channelId}`;
    this.topicSubscription = this.ws.rxStomp.watch(topic).subscribe((message) => {
      const receivedMsg: Message = JSON.parse(message.body);
      
      if (!this.messages.some(m => m.id === receivedMsg.id)) {
        this.messages.push(receivedMsg);
        this.cdr.detectChanges();
        setTimeout(() => this.scrollToBottom(), 50);
      }
    });
  }

  sendMessage(): void {
    if (!this.chatInput.trim() || !this.activeChannelId) return;

    const payload: Message = {
      content: this.chatInput,
      channelId: this.activeChannelId as any,
      sendId: this.myUserId
    };

    const tempInput = this.chatInput;
    this.chatInput = ''; 

    this.messageService.sendMessage(payload).subscribe({
      next: (savedMsg) => {
        if (!this.messages.some(m => m.id === savedMsg.id)) {
          this.messages.push(savedMsg);
          this.cdr.detectChanges();
          setTimeout(() => this.scrollToBottom(), 50);
        }
      },
      error: (err) => {
        console.error('Error sending message', err);
        this.chatInput = tempInput;
      }
    });
  }

  // ==========================================
  // 🚀 LÓGICA DEL MODAL CLON (Crear Canal)
  // ==========================================
  
  isCreateChannelModalOpen: boolean = false;

  openCreateChannelModal(): void {
    this.isCreateChannelModalOpen = true;
  }

  onChannelCreated(newChannel: any): void {
    this.isCreateChannelModalOpen = false; 
    
    if (this.serverData) {
      if (!this.serverData.channels) {
        this.serverData.channels = [];
      }
      this.serverData.channels.push(newChannel);
    }
    
    if (newChannel.type === 'TEXT') {
      this.selectChannel(newChannel.id);
    }
    
    this.cdr.detectChanges();
  }

  private scrollToBottom(): void {
    try {
      if (this.scrollContainer) {
        this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
      }
    } catch (err) {}
  }

  getActiveChannelName(): string {
    if (!this.serverData?.channels || !this.activeChannelId) return 'general';
    const channel = this.serverData.channels.find(c => c.id === this.activeChannelId);
    return channel?.name || 'general';
  }

  isOwner(): boolean {
    if (!this.serverData || !this.serverData.ownerId || !this.myUserId) return false;
    return Number(this.serverData.ownerId) === Number(this.myUserId);
  }

  // ==========================================
  // 💥 LÓGICA DEL MODAL ELIMINAR SERVIDOR
  // ==========================================
  isDeleteServerModalOpen: boolean = false;

  openDeleteServerModal(): void {
    this.isServerMenuOpen = false; // Cerramos el menú desplegable superior
    this.isDeleteServerModalOpen = true; // Abrimos el modal oscuro
  }

  /**
   * Esta función se ejecuta ÚNICAMENTE cuando el modal hijo nos confirma 
   * que el usuario escribió el nombre exacto y apretó el botón rojo.
   */
  executeServerDeletion(): void {
    if (!this.currentServerId) return;

    this.serverService.deleteServer(this.currentServerId).subscribe({
      next: () => {
        this.isDeleteServerModalOpen = false;
        window.dispatchEvent(new CustomEvent('server-joined'));
        this.router.navigate(['/home']);
      },
      error: (err) => console.error('Error al eliminar el servidor', err)
    });
  }

  leaveServer(): void {
    if (!this.currentServerId) return;

    this.serverService.leaveServer(this.currentServerId).subscribe({
      next: () => {
        window.dispatchEvent(new CustomEvent('server-joined'));
        this.router.navigate(['/home']);
      },
      error: (err) => console.error('failed leaving the server', err)
    });
  }

  toggleServerMenu(): void { this.isServerMenuOpen = !this.isServerMenuOpen; }
  toggleMembersList(): void { this.isMembersListOpen = !this.isMembersListOpen; }

  ngOnDestroy(): void {
    this.topicSubscription?.unsubscribe();
    this.routeSub?.unsubscribe();
  }
}