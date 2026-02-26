import { Component, Input, OnChanges, SimpleChanges, ChangeDetectorRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { MessageControllerService } from '../../../../../../services/api';
import { Message } from '../../../../../../services/api';

@Component({
  selector: 'app-chat-room',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-room.html',
  styleUrl: './chat-room.css',
})
export class ChatRoom {

  @Input({required: true}) conversationId!: number;
  @Input({required: true}) friendName!: string;

  newMessage: string = '';
  messages: Message[] = [];

  myUserId: number = 1;

  constructor(
    private messageService: MessageControllerService,
    private cdr: ChangeDetectorRef
  ){}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['conversationId'] && this.conversationId) {
      this.loadChatHistory();
    }
  }

  loadChatHistory(){

    this.messageService.getChatHistory(this.conversationId as any).subscribe({
       next:(history) => {
        this.messages = history;
        this.cdr.markForCheck();
        this.scrollToBottom();
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

      this.messageService.sendMessage(msgPayload as any).subscribe({
        next: (savedMessage) =>{
          this.messages.push(savedMessage);
          this.newMessage = '';
          this.cdr.markForCheck();
          this.scrollToBottom();
        },
        error: (err) => {
          console.error('Failed to send message', err);
        }
      });
    }

    getSenderName(sendId: number | undefined): string {
      if(!sendId) return 'Unknown';
      return sendId === this.myUserId ? 'You' : this.friendName;
    }

    scrollToBottom() {
      setTimeout(() => {
        const chatContainer = document.querySelector('.chat-scroll-container');
        if (chatContainer) {
          chatContainer.scrollTop = chatContainer.scrollHeight;
        }
      }, 100);
    }

  @Output() onClose = new EventEmitter<void>();

  backToFriends() {
  this.onClose.emit();
  }
}
