import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { ServerControllerService, ServerResponse, MessageControllerService, Message, AuthenticationService } from '../../../../../services/api';
import { Websocket } from '../../../../../services/api/websocket/websocket';
import { AuthService } from '../../../../../services/api/authservice/auth-service';
import { Token } from '../../../../../services/api/token/token';

// 🚀 IMPORTAMOS EL NUEVO COMPONENTE (Ajusta la ruta si es necesario)
import { CreateChannelModal } from '../../../components/modals/create-channel-modal/create-channel-modal/create-channel-modal';

/**
 * Componente principal para la gestión de servidores.
 * Controla la visualización de canales, lista de miembros y la lógica de chat en tiempo real.
 */
@Component({
  selector: 'app-server',
  standalone: true,
  // 🚀 AÑADIMOS EL MODAL A LOS IMPORTS DEL COMPONENTE
  imports: [CommonModule, RouterModule, FormsModule, CreateChannelModal],
  templateUrl: './server.html',
  styleUrl: './server.css',
})
export class Server implements OnInit, OnDestroy {
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  /**
   * Compara de forma segura si el mensaje es del usuario actual.
   * Usamos Number() para evitar errores si uno es String y el otro Number.
   */
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

  /** Suscripción activa al topic de WebSocket del canal */
  private topicSubscription?: Subscription;
  /** Suscripción a los cambios de parámetros de la URL */
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
    // Recuperamos el ID del usuario desde nuestro servicio centralizado
    this.myUserId = this.authService.getMyUserId()
  }

  ngOnInit(): void {
    // Escuchamos cambios en la URL (al cambiar de un servidor a otro)
    this.routeSub = this.route.paramMap.subscribe(param => {
      const idStr = param.get('serverId');
      if (idStr) {
        this.currentServerId = Number(idStr);
        this.loadServerData(this.currentServerId);
      }
    });
  }

  /**
   * Carga la información completa del servidor y selecciona el primer canal por defecto.
   * @param id ID del servidor a cargar.
   */
  loadServerData(id: number): void {
    this.serverData = null;
    this.isServerMenuOpen = false;

    this.messages = [];
    this.activeChannelId = null;

    this.serverService.findServerById(id).subscribe({
      next: (data: ServerResponse) => {
        this.serverData = data;
        if (data.channels && data.channels.length > 0) {
          // Al entrar al servidor, seleccionamos el primer canal automáticamente
          this.selectChannel(data.channels[0].id);
        } else {
          this.activeChannelId = null;
        }
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Failed to load server', err)
    });
  }

  /**
   * Cambia el canal activo, carga su historial y actualiza la suscripción al WebSocket.
   * @param channelId ID del canal seleccionado.
   */
  selectChannel(channelId: number | undefined): void {
    if (channelId === undefined) return;
    
    this.activeChannelId = channelId;
    this.loadChannelHistory(channelId);
    this.connectToChannelTopic(channelId);
  }

  /**
   * Carga el historial de mensajes de MongoDB para el canal específico.
   */
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

  /**
   * Gestiona la conexión en tiempo real mediante WebSockets (vía Kafka).
   */
  private connectToChannelTopic(channelId: number): void {
    if (this.topicSubscription) {
      this.topicSubscription.unsubscribe();
    }

    const topic = `/topic/channel.${channelId}`;
    this.topicSubscription = this.ws.rxStomp.watch(topic).subscribe((message) => {
      const receivedMsg: Message = JSON.parse(message.body);
      
      // Verificación de duplicados para evitar eco del socket
      if (!this.messages.some(m => m.id === receivedMsg.id)) {
        this.messages.push(receivedMsg);
        this.cdr.detectChanges();
        setTimeout(() => this.scrollToBottom(), 50);
      }
    });
  }

  /**
   * Envía un nuevo mensaje al canal actual.
   */
  sendMessage(): void {
    if (!this.chatInput.trim() || !this.activeChannelId) return;

    const payload: Message = {
      content: this.chatInput,
      channelId: this.activeChannelId as any, // Discriminador para el backend
      sendId: this.myUserId
    };

    const tempInput = this.chatInput;
    this.chatInput = ''; // Limpieza rápida del input (Optimistic UI)

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
        this.chatInput = tempInput; // Revertimos en caso de error
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

  /**
   * Recibe el evento emitido por el componente hijo (el modal)
   * con el objeto del nuevo canal ya guardado en la base de datos.
   */
  onChannelCreated(newChannel: any): void {
    // 1. Cerramos el modal
    this.isCreateChannelModalOpen = false; 
    
    // 2. Inyectamos visualmente el canal en la barra lateral
    if (this.serverData) {
      if (!this.serverData.channels) {
        this.serverData.channels = [];
      }
      this.serverData.channels.push(newChannel);
    }
    
    // 3. Si el canal es de texto, saltamos automáticamente a él para empezar a chatear
    if (newChannel.type === 'TEXT') {
      this.selectChannel(newChannel.id);
    }
    
    this.cdr.detectChanges();
  }


  /**
   * Realiza el scroll automático al final del contenedor de mensajes.
   */
  private scrollToBottom(): void {
    try {
      if (this.scrollContainer) {
        this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
      }
    } catch (err) {}
  }

  /**
   * Obtiene el nombre del canal activo para mostrarlo en la cabecera.
   */
  getActiveChannelName(): string {
    if (!this.serverData?.channels || !this.activeChannelId) return 'general';
    const channel = this.serverData.channels.find(c => c.id === this.activeChannelId);
    return channel?.name || 'general';
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