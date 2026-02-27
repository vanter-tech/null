import { Component, OnInit, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConversationControllerService } from '../../../../services/api';
import { ConversationResponse } from '../../../../services/api';


@Component({
  selector: 'app-dm-sidebar',
  imports: [CommonModule],
  templateUrl: './dm-sidebar.html',
  styleUrl: './dm-sidebar.css',
})
export class DmSidebar implements OnInit {

  conversations: ConversationResponse[] = [];

  @Output() onConversationSelect = new EventEmitter<ConversationResponse>();

  constructor(
    private conversationService: ConversationControllerService,
    private cdr: ChangeDetectorRef
  ){}

  ngOnInit(): void {
      this.loadConversation();
      
  }

  loadConversation(){

    this.conversationService.getConversation().subscribe({
      next: (data) => {
        this.conversations = data
        this.cdr.detectChanges();
        
      },
      error: (err) => console.log("Cant load chat rooms", err)
      
    });

  }

  selectConversation(conv: ConversationResponse){
    this.onConversationSelect.emit(conv)
  }

}
