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

import { Track, RoomEvent, Room } from 'livekit-client'
import { VoiceControllerService } from '../../../../../services/api/api/voiceController.service';
import { VoiceControlPanel } from './components/voice-control-panel/voice-control-panel';
import { VoiceRoom } from './components/voice-room/voice-room';

/**
 * Componente principal para la gestión de servidores.
 * Controla la visualización de canales, lista de miembros y la lógica de chat en tiempo real.
 */
@Component({
  selector: 'app-server',
  standalone: true,
  imports: [CommonModule, 
    RouterModule, 
    FormsModule, 
    CreateChannelModal, 
    DeleteServerModal,
    VoiceControlPanel,
    VoiceRoom
  
  ],
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

  currentVoiceRoom: Room | null = null;

  // 👥 Lista de personas conectadas al audio
  voiceParticipants: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private serverService: ServerControllerService,
    private messageService: MessageControllerService,
    private authService: AuthService,
    private ws: Websocket,
    private cdr: ChangeDetectorRef,
    private tokenService: Token,
    private voiceService: VoiceControllerService
  ) {
    this.myUserId = this.authService.getMyUserId()
  }

  ngOnInit(): void {

    this.ws.conectar();

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
    if (channelId === undefined || !this.serverData?.channels) return;
    
    // Buscamos el canal completo para saber su tipo
    const channel = this.serverData.channels.find(c => c.id === channelId);
    if (!channel) return;

    this.activeChannelId = channelId;

    if (channel.type === 'VOICE') {
      // 🎙️ ES UN CANAL DE VOZ
      console.log('🎙️ Entrando a canal de voz:', channel.name);
      this.joinVoiceChannel(channelId);
    } else {
      // 💬 ES UN CANAL DE TEXTO
      this.leaveVoiceChannel(); // Apagamos el micro si veníamos de un canal de voz
      this.loadChannelHistory(channelId);
      this.connectToChannelTopic(channelId);
    }
  }

  // 🚀 Helper para saber si dibujamos el chat o la sala de voz
  getActiveChannelType(): string {
    if (!this.serverData?.channels || !this.activeChannelId) return 'TEXT';
    const channel = this.serverData.channels.find(c => c.id === this.activeChannelId);
    return channel?.type || 'TEXT';
  }


  joinVoiceChannel(channelId: number): void {
    this.leaveVoiceChannel(); // Limpieza previa

    console.log(`🎟️ Solicitando boleto VIP para el canal de voz ${channelId}...`);

    this.voiceService.getVoiceToken(channelId.toString()).subscribe({
      next: async (response: any) => {
        const token = response.token || response['token']; 
        if (token) {
          await this.connectToLiveKit(token);
        }
      },
      error: (err) => console.error('❌ Error al obtener el token de voz', err)
    });
  }

  async connectToLiveKit(token: string) {
    this.currentVoiceRoom = new Room();

    // 🚀 AÑADIDO: Escuchar quién entra, quién sale y quién está hablando
    this.currentVoiceRoom.on(RoomEvent.ParticipantConnected, () => this.updateParticipants());
    this.currentVoiceRoom.on(RoomEvent.ParticipantDisconnected, () => this.updateParticipants());
    this.currentVoiceRoom.on(RoomEvent.ActiveSpeakersChanged, () => this.updateParticipants());

    // 🎧 Escuchar cuando alguien habla
    this.currentVoiceRoom.on(RoomEvent.TrackSubscribed, (track, publication, participant) => {
      if (track.kind === Track.Kind.Audio) {
        console.log(`🔊 Recibiendo audio de: ${participant.identity}`);
        const audioElement = track.attach();
        document.body.appendChild(audioElement);
      }
    });

    this.currentVoiceRoom.on(RoomEvent.TrackUnsubscribed, (track) => {
      track.detach(); 
    });

    try {
      // 🌐 Conectar al Docker
      const livekitUrl = 'ws://127.0.0.1:7880';
      await this.currentVoiceRoom.connect(livekitUrl, token);
      console.log('✅ Conectados exitosamente al servidor LiveKit');

      // 🚀 AÑADIDO: Actualizamos la lista por primera vez al entrar
      this.updateParticipants();

      // 🎙️ Encender nuestro micrófono
      await this.currentVoiceRoom.localParticipant.setMicrophoneEnabled(true, {
        echoCancellation: true,
        noiseSuppression: true,
        autoGainControl: true,
        channelCount: 1, 
      });
      
      console.log('🎙️ Micrófono activado y transmitiendo al canal');

    } catch (error) {
      console.error('❌ Error fatal al conectar con LiveKit:', error);
      alert('No se pudo conectar al servidor de voz. Verifica que Docker esté corriendo.');
    }
  }

  leaveVoiceChannel() {
    if (this.currentVoiceRoom) {
      this.currentVoiceRoom.disconnect();
      this.currentVoiceRoom = null;
      console.log('🔇 Desconectado del canal de voz');
      this.cdr.detectChanges();
    }
  }

  // 🚀 Actualiza la lista visual de avatares cada vez que alguien entra, sale o habla
  updateParticipants() {
    if (!this.currentVoiceRoom) return;
    
    // Combinamos tu usuario local con los usuarios remotos
    const participants = [
      this.currentVoiceRoom.localParticipant,
      ...Array.from(this.currentVoiceRoom.remoteParticipants.values())
    ];
    
    // Mapeamos los datos para que el componente voice-room los entienda fácil
    this.voiceParticipants = participants.map(p => ({
      identity: p.identity,
      isSpeaking: p.isSpeaking,
      isLocal: p === this.currentVoiceRoom?.localParticipant
    }));
    
    this.cdr.detectChanges();
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

    const topic = `/topic/channel/${channelId}`;
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

  getServerName(): string{
    if(!this.serverData?.name || !this.activeChannelId) return 'MyServer';
    const server = this.serverData.name
    return server
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
    this.leaveVoiceChannel();
  }
}