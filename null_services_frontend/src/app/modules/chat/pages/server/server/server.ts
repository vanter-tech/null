import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, OnDestroy, NgZone, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { ServerControllerService, ServerResponse, MessageControllerService, Message, ChannelResponse } from '../../../../../services/api';
import { Websocket } from '../../../../../services/api/websocket/websocket';
import { AuthService } from '../../../../../services/api/authservice/auth-service';
import { Token } from '../../../../../services/api/token/token';

import { CreateChannelModal } from '../../../components/modals/create-channel-modal/create-channel-modal/create-channel-modal';
import { DeleteServerModal } from '../../../components/modals/delete-server-modal/delete-server-modal/delete-server-modal';

import { Track, RoomEvent, Room } from 'livekit-client'
import { VoiceControllerService } from '../../../../../services/api/api/voiceController.service';
import { VoiceControlPanel } from './components/voice-control-panel/voice-control-panel';
import { VoiceRoom } from './components/voice-room/voice-room';

import { MemberSidebar } from '../../../components/member-sidebar/member-sidebar/member-sidebar';

// Interfaces STOMP
export interface VoiceJoinRequest {
  channelId: number;
  userId: number;
  username: string;
  imageUrl: string;
}

export interface VoiceParticipant {
  userId: number;
  username: string;
  imageUrl: string;
}

export interface ActiveVoiceParticipant {
  identity: string;
  isSpeaking: boolean;
  isLocal: boolean;
}

@Component({
  selector: 'app-server',
  standalone: true,
  imports: [CommonModule, 
    RouterModule, 
    FormsModule, 
    CreateChannelModal, 
    DeleteServerModal,
    VoiceControlPanel,
    VoiceRoom,
    MemberSidebar
  ],
  templateUrl: './server.html',
  styleUrl: './server.css',
})
export class Server implements OnInit, OnDestroy {
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  isItMe(sendId: number | undefined): boolean {
    if (!sendId || !this.myUserId) return false;
    return Number(sendId) === Number(this.myUserId);
  }
  
  currentServerId: number | null = null;
  serverData: ServerResponse | null = null;
  activeChannelId: number | null = null;
  activeChannelName = 'general'; 
  activeChannelType = 'TEXT';
  
  isMembersListOpen = true;
  isServerMenuOpen = false;

  messages: Message[] = [];
  chatInput = '';
  myUserId!: number;

  private topicSubscription?: Subscription;
  private routeSub?: Subscription;

  currentVoiceRoom: Room | null = null;
  connectedVoiceChannelId: number | null = null; 

  voiceParticipants: ActiveVoiceParticipant[] = [];

  globalVoiceState: Record<number, VoiceParticipant[]> = {};
  private voicePresenceSub?: Subscription;

  private route = inject(ActivatedRoute)
  private router = inject(Router)
  private serverService = inject(ServerControllerService)
  private messageService = inject(MessageControllerService)
  private authService = inject(AuthService)
  private ws = inject(Websocket)
  private cdr = inject(ChangeDetectorRef)
  private tokenService = inject(Token)
  private voiceService = inject(VoiceControllerService)
  private ngZone = inject(NgZone)

  constructor() {
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


  

  private connectToVoicePresence(serverId: number): void {
    if (this.voicePresenceSub) {
      this.voicePresenceSub.unsubscribe();
    }

    const topic = `/topic/server/${serverId}/voice-presence`;
    this.voicePresenceSub = this.ws.rxStomp.watch(topic).subscribe((message) => {
      // 🚀 Envolvemos en NgZone para repintar la barra lateral global
      this.ngZone.run(() => {
        this.globalVoiceState = JSON.parse(message.body);
        this.cdr.detectChanges();
      });
    });

    setTimeout(() => {
      this.ws.rxStomp.publish({ destination: `/app/server/${serverId}/voice/sync` });
    }, 300);
  }

  loadServerData(id: number): void {
    this.serverData = null;
    this.isServerMenuOpen = false;
    this.messages = [];
    this.activeChannelId = null;

    this.connectToVoicePresence(id); 

    this.serverService.findServerById(id).subscribe({
      next: (data: ServerResponse) => {
        this.serverData = data;
        if (data.channels && data.channels.length > 0) {
          this.selectChannel(data.channels[0].id);
        } else {
          this.activeChannelId = null;
          this.activeChannelName = 'general';
          this.activeChannelType = 'TEXT';
        }
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Failed to load server', err)
    });
  }

  selectChannel(channelId: number | undefined): void {
    if (channelId === undefined || !this.serverData?.channels) return;
    
    const channel = this.serverData.channels.find(c => Number(c.id) === Number(channelId));
    if (!channel) return;

    this.activeChannelId = channelId;
    this.activeChannelName = channel.name || 'general'; 
    this.activeChannelType = channel.type || 'TEXT';

    this.messages = [];

    if (channel.type === 'VOICE') {
      console.log('🎙️ Entrando a canal de voz:', channel.name);
      this.joinVoiceChannel(this.activeChannelId);
    } else {
      this.leaveVoiceChannel(); 
      this.loadChannelHistory(this.activeChannelId);
      this.connectToChannelTopic(this.activeChannelId);
    }

    this.cdr.detectChanges(); 
  }

  getActiveChannelType(): string {
    if (!this.serverData?.channels || !this.activeChannelId) return 'TEXT';
    const channel = this.serverData.channels.find(c => Number(c.id) === Number(this.activeChannelId));
    return channel?.type || 'TEXT';
  }

  joinVoiceChannel(channelId: number): void {
    if (this.connectedVoiceChannelId === channelId) return;

    this.leaveVoiceChannel(); 
    
    this.connectedVoiceChannelId = channelId;

    console.log(`🎟️ Solicitando boleto VIP para el canal de voz ${channelId}...`);

    if (this.currentServerId) {
      const myMemberData = this.serverData?.members?.find((m) => Number(m.id) === Number(this.myUserId));
      const realUsername = myMemberData?.username || 'Usuario ' + this.myUserId;
      const realImageUrl = myMemberData?.imageUrl || '';

      const joinRequest: VoiceJoinRequest = {
        channelId: channelId,
        userId: this.myUserId,
        username: realUsername, 
        imageUrl: realImageUrl
      };

      this.ws.rxStomp.publish({
        destination: `/app/server/${this.currentServerId}/voice/join`,
        body: JSON.stringify(joinRequest)
      });
    }

    this.voiceService.getVoiceToken(channelId.toString()).subscribe({
      next: async (response: Record<string, string>) => {
        const token = response['token']; 
        if (token) {
          await this.connectToLiveKit(token);
        }
      },
      error: (err) => console.error('❌ Error al obtener el token de voz', err)
    });
  }

  isUserSpeaking(userId: number, username: string): boolean {
    if (!this.currentVoiceRoom || this.voiceParticipants.length === 0) return false;

    if (this.isItMe(userId)) {
      const myLocalAudio = this.voiceParticipants.find(vp => vp.isLocal);
      return myLocalAudio ? myLocalAudio.isSpeaking : false;
    }

    const remoteAudio = this.voiceParticipants.find(vp => 
      vp.identity === username || 
      vp.identity.includes(username) 
    );
    return remoteAudio ? remoteAudio.isSpeaking : false;
  }

  async connectToLiveKit(token: string) {
    this.currentVoiceRoom = new Room();

    // 🚀 Envolvemos los eventos de LiveKit en NgZone para repintar el cuarto de voz central
    this.currentVoiceRoom.on(RoomEvent.ParticipantConnected, () => this.ngZone.run(() => this.updateParticipants()));
    this.currentVoiceRoom.on(RoomEvent.ParticipantDisconnected, () => this.ngZone.run(() => this.updateParticipants()));
    this.currentVoiceRoom.on(RoomEvent.ActiveSpeakersChanged, () => this.ngZone.run(() => this.updateParticipants()));

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
      const livekitUrl = 'ws://127.0.0.1:7880';
      await this.currentVoiceRoom.connect(livekitUrl, token);
      console.log('✅ Conectados exitosamente al servidor LiveKit');

      this.updateParticipants();

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
      
      if (this.connectedVoiceChannelId && this.currentServerId) {
        this.ws.rxStomp.publish({
          destination: `/app/server/${this.currentServerId}/voice/leave`,
          body: JSON.stringify({
            channelId: this.connectedVoiceChannelId,
            userId: this.myUserId
          })
        });
      }

      this.currentVoiceRoom.disconnect();
      this.currentVoiceRoom = null;
      this.connectedVoiceChannelId = null; 

      this.voiceParticipants = [];

      console.log('🔇 Desconectado del canal de voz');
      this.cdr.detectChanges();
    }
  }

  updateParticipants() {
    if (!this.currentVoiceRoom) return;
    
    const participants = [
      this.currentVoiceRoom.localParticipant,
      ...Array.from(this.currentVoiceRoom.remoteParticipants.values())
    ];
    
    this.voiceParticipants = participants.map(p => ({
      identity: p.identity,
      isSpeaking: p.isSpeaking,
      isLocal: p === this.currentVoiceRoom?.localParticipant
    }));
    
    this.cdr.detectChanges();
  }

  // 🚀 NUEVO: Decide qué lista mostrar en la pantalla central dependiendo de si estamos en la llamada o no
  getDisplayVoiceParticipants(): ActiveVoiceParticipant[] {
    // 1. Si estamos conectados a LiveKit, mostramos la lista en tiempo real (con aros verdes)
    if (this.currentVoiceRoom) {
      return this.voiceParticipants;
    }
    
    // 2. Si estamos desconectados (viendo desde afuera), usamos la lista global de Spring Boot
    if (this.activeChannelId && this.globalVoiceState[this.activeChannelId]) {
      return this.globalVoiceState[this.activeChannelId].map(p => ({
        identity: p.username,
        isSpeaking: false, // Como estamos afuera, no sabemos quién habla exactamente
        isLocal: false
      }));
    }

    return [];
  }

  private loadChannelHistory(channelId: number): void {
    this.messageService.getChannelHistory(channelId).subscribe({
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
      channelId: this.activeChannelId,
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

  isCreateChannelModalOpen = false;

  openCreateChannelModal(): void {
    this.isCreateChannelModalOpen = true;
  }

  onChannelCreated(newChannel: ChannelResponse): void {
    this.isCreateChannelModalOpen = false; 
    
    if (this.serverData) {
      if (!this.serverData.channels) {
        this.serverData.channels = [];
      }
      this.serverData.channels = [...this.serverData.channels, newChannel];
    }

    const channelType = newChannel.type?.toUpperCase();
    
    if (channelType === 'TEXT' || channelType === 'VOICE') {
      this.selectChannel(newChannel.id);
    }
    
    this.cdr.detectChanges();
  }

  private scrollToBottom(): void {
    try {
      if (this.scrollContainer) {
        this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
      }
    } catch {
      // Intentionally ignore scroll errors
    }
  }

  getActiveChannelName(): string {
    if (!this.serverData?.channels || !this.activeChannelId) return 'general';
    const channel = this.serverData.channels.find(c => Number(c.id) === Number(this.activeChannelId));
    return channel?.name || 'general';
  }

  getServerName(): string{
    return this.serverData?.name || 'Cargando...';
  }

  isOwner(): boolean {
    if (!this.serverData || !this.serverData.ownerId || !this.myUserId) return false;
    return Number(this.serverData.ownerId) === Number(this.myUserId);
  }

  isDeleteServerModalOpen = false;

  openDeleteServerModal(): void {
    this.isServerMenuOpen = false; 
    this.isDeleteServerModalOpen = true; 
  }

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
    this.voicePresenceSub?.unsubscribe(); 
    this.leaveVoiceChannel();
  }
}