import { ElementRef, ViewChild, 
  Component, Input, OnChanges, SimpleChanges, ChangeDetectorRef, Output, EventEmitter, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';


import { Token } from '../../../../../../../services/api/token/token';

import { Subscription } from 'rxjs';

import { MessageControllerService } from '../../../../../../../services/api';
import { Message } from '../../../../../../../services/api';;
import { Websocket } from '../../../../../../../services/api/websocket/websocket';



@Component({
  selector: 'app-chat-room',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-room.html',
  styleUrl: './chat-room.css',
})
export class ChatRoom implements OnChanges, OnDestroy {

  @Input({required: true}) conversationId!: number;
  @Input({required: true}) friendName!: string;
  @ViewChild('scrollMe') private myScrollContainer!: ElementRef;

  newMessage: string = '';
  messages: Message[] = [];

  myUserId!: number;

  private topicSubscription?: Subscription;

  // 🚀 Objeto temporal para el panel lateral (Después lo traerás de tu Backend)
  friendProfile = {
    avatar: '', 
    bannerColor: '#5865F2', 
    pronouns: 'Él/Lo',
    bio: 'Resolviendo problemas de WebSockets a altas horas de la noche. ☕',
    memberSince: 'Marzo 2026',
    mutualServers: 1
  };

  constructor(
    private messageService: MessageControllerService,
    private wsService: Websocket,
    private cdr: ChangeDetectorRef,
    private tokenService: Token
  ){
    this.extractIdFromToken();
  }

  extractIdFromToken(){
    const tokenStr = this.tokenService.token
    if(tokenStr){
      try{
        const payload = JSON.parse(atob(tokenStr.split('.')[1]))
        console.log('Token conten:' , payload);

        this.myUserId = payload.userId;
        
      }catch(e){
        console.log('Decodifing token error', e);
        
      }
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['conversationId'] && this.conversationId) {

      this.wsService.conectar();

      this.loadChatHistory();
      this.connectToRoom();
    }
  }

  connectToRoom() {

    if (this.topicSubscription) {
      this.topicSubscription.unsubscribe();
    }

    const topic = `/topic/chat/${this.conversationId}`;

    this.topicSubscription = this.wsService.rxStomp.watch(topic).subscribe((message) => {
      const receivedMsg: Message = JSON.parse(message.body);
      
      // 🛡️ SOLUCIÓN 1: Evitamos duplicados verificando si el ID ya existe en pantalla
      const alreadyExists = this.messages.some(m => m.id === receivedMsg.id);
      
      if (!alreadyExists) {
        this.messages.push(receivedMsg);
        this.cdr.detectChanges();
        
        // 📜 SOLUCIÓN 2: Retrasamos el scroll 50ms para que Angular alcance a dibujar el HTML
        setTimeout(() => this.scrollToBottom(), 50);
      }
    });

  }

  ngOnDestroy() {
    if (this.topicSubscription) {
      this.topicSubscription.unsubscribe();
    } 
  }

  loadChatHistory(){

    this.messageService.getChatHistory(this.conversationId as any).subscribe({
       next:(history) => {
        this.messages = history;
        this.cdr.markForCheck();
        setTimeout(() => this.scrollToBottom());
       },
      error: (err) => {
         console.error('Failed to load chat history', err);
        }
      });

    }

  sendMessage() {

      if (!this.newMessage.trim()) return;

    const msgPayload: any = {
      content: this.newMessage,
      conversationId: this.conversationId,
      sendId: this.myUserId, 
    };

    // Vaciamos el input de inmediato para que se sienta súper rápido
    this.newMessage = ''; 

    this.messageService.sendMessage(msgPayload).subscribe({
      next: (savedMessage) => {
        
        // 🛡️ SOLUCIÓN 1: Evitamos que choque con el mensaje que viene del WebSocket
        const alreadyExists = this.messages.some(m => m.id === savedMessage.id);
        
        if (!alreadyExists) {
          this.messages.push(savedMessage);
          this.cdr.detectChanges();
          
          // 📜 SOLUCIÓN 2: Scroll fluido hacia abajo
          setTimeout(() => this.scrollToBottom(), 50);
        }
      },
      error: (err) => console.error('Error enviando el mensaje:', err)
    });

  }


  getSenderName(msg: Message): string {
  // 1. Si el ID del remitente es el mío, ponemos "Tú"
  if (msg.sendId === this.myUserId) {
    return 'Tú';
  }

  // 2.  LA SOLUCIÓN: Usamos el nickname que inyectaste en el MessageService de Java
  if (msg.senderNickname) {
    return msg.senderNickname;
  }

  // 3. FALLBACK: Si por alguna razón el nickname viene vacío, 
  // intentamos usar el friendName (que en grupos es la lista de nombres)
  // o un genérico si todo falla.
  return this.friendName || 'Usuario';
}

  scrollToBottom(): void {
    try {
      // Le decimos al contenedor que su posición superior sea igual a su altura total
      this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight;
    } catch(err) { 
      // Silenciamos el error por si Angular intenta hacer scroll antes de que el contenedor exista
    }
  }


  @Output() onClose = new EventEmitter<void>();

  backToFriends() {
  this.onClose.emit();
  }
}
