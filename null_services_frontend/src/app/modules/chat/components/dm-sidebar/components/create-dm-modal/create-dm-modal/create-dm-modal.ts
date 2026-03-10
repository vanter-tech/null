import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

// 🚀 IMPORTACIONES CORRECTAS DE TU API
import { FriendResponseDTO, ConversationControllerService } from '../../../../../../../services/api';
import { FriendsDataService } from '../../../../../../../services/api/friends-data-service/friends-data-service';
import { Modalservice } from '../../../../../../../services/api/modalservice/modalservice';

import { ChatNavigationService } from '../../../../../../../services/api/chat-navigation-service/chat-navigation-service';

@Component({
  selector: 'app-create-dm-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-dm-modal.html',
  // Si estás usando 100% Tailwind en el HTML, puedes quitar el styleUrl si el archivo .css está vacío
})
export class CreateDmModalComponent implements OnInit, OnDestroy {
  
  // Estado
  friendsList: FriendResponseDTO[] = [];
  selectedFriendIds = new Set<number>();
  searchTerm: string = '';
  private sub?: Subscription;

  constructor(
    private friendsDataService: FriendsDataService,
    private modalService: Modalservice,
    private conversationControllerService: ConversationControllerService, // 🚀 AHORA SÍ ES TU SERVICIO REAL
    private router: Router,
    private chatNavigationService: ChatNavigationService
  ) {}

  ngOnInit(): void {
    // 1. Nos suscribimos a la fuente de datos compartida
    this.sub = this.friendsDataService.allFriends$.subscribe(friends => {
      this.friendsList = friends;
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  get filteredFriends(): FriendResponseDTO[] {
    if (!this.searchTerm.trim()) return this.friendsList;
    
    const searchLower = this.searchTerm.toLowerCase();
    return this.friendsList.filter(f => 
      f.name?.toLowerCase().includes(searchLower)
    );
  }

  toggleSelection(friendId: number | undefined): void {
    if (friendId === undefined) return;
    
    
    if (this.selectedFriendIds.has(friendId)) {
      this.selectedFriendIds.delete(friendId);
    } else {
      this.selectedFriendIds.add(friendId);
    }
  }

  isFriendSelected(friendId: number | undefined): boolean {
    if (friendId === undefined) return false;
    return this.selectedFriendIds.has(friendId);
  }

  createDirectMessage(): void {
    if (this.selectedFriendIds.size === 0) return;

    const recipientIds = Array.from(this.selectedFriendIds);

    // ==========================================
    // 👤 CASO 1: CHAT 1 VS 1
    // ==========================================
    if (recipientIds.length === 1) {
      this.executeCreation(recipientIds);
      return;
    }

    // ==========================================
    // 👥 CASO 2: CHAT GRUPAL (Validación)
    // ==========================================
    console.log('Validando si el grupo ya existe...');
    
    // Obtenemos las conversaciones actuales para comparar
    this.conversationControllerService.getConversation().subscribe({
      next: (conversations) => {
        
        const isDuplicate = this.checkIfGroupExists(recipientIds, conversations);
        
        if (isDuplicate) {
          const confirmNew = confirm("Ya tienes un grupo con estas mismas personas. ¿Quieres crear uno nuevo de todas formas?");
          if (!confirmNew) return; // Si el usuario cancela, detenemos todo
        }

        // Si no es duplicado o el usuario aceptó crear otro, procedemos
        this.executeCreation(recipientIds);
      },
      error: (err) => {
        console.error("Error validando grupos, intentando crear de todos modos...", err);
        this.executeCreation(recipientIds);
      }
    });
  }

  /**
   * 🧠 Función que compara los nombres seleccionados con los chats existentes
   */
  private checkIfGroupExists(newIds: number[], conversations: any[]): boolean {
    // 1. Obtenemos los nombres de los amigos que seleccionaste y los ordenamos alfabéticamente
    const selectedNames = newIds.map(id => {
      const friend = this.friendsList.find(f => f.id === id);
      return friend ? friend.name : '';
    }).filter(name => name !== '').sort();

    // 2. Buscamos en las conversaciones que vinieron del backend
    for (const conv of conversations) {
      if (conv.otherUserName) {
        // El backend devuelve ej: "Test2, Test3". Lo separamos en un array y lo ordenamos
        const convNames = conv.otherUserName.split(',').map((n: string) => n.trim()).sort();

        // 3. Comparamos si ambos arrays son exactamente iguales
        if (JSON.stringify(selectedNames) === JSON.stringify(convNames)) {
          return true; // ¡Alerta! Ya existe este grupo
        }
      }
    }
    
    return false; // Vía libre, no existe
  }

  /**
   * 🚀 Función que realmente llama a tu API para crear el chat
   */
  private executeCreation(ids: number[]) {
    if (ids.length === 1) {
      // API para 1 vs 1
      this.conversationControllerService.createConversation(ids[0] as any).subscribe({
        next: (response: any) => {
          this.closeModal();
          const name = this.friendsList.find(f => f.id === ids[0])?.name || response.otherUserName || 'Usuario';
          this.chatNavigationService.openChat(response.id, name);
        },
        error: (err) => console.error('Error al crear DM 1v1', err)
      });
    } else {
      // API para Grupos
      this.conversationControllerService.createGroupConversation(ids).subscribe({
        next: (response: any) => {
          this.closeModal();
          const groupName = response.otherUserName || 'Grupo Nuevo';
          this.chatNavigationService.openChat(response.id, groupName);
        },
        error: (err) => console.error('Error al crear DM Grupal', err)
      });
    }
  }

  closeModal(): void {
    this.modalService.closeCreateDm(); 
  }
}