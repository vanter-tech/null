import { Component, Input, OnChanges, SimpleChanges, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule, UpperCasePipe } from '@angular/common';
import { Subscription } from 'rxjs';

import { MemberProfilePopUp } from './components/member-profile-pop-up/member-profile-pop-up/member-profile-pop-up';
import { PresenceService } from '../../../../../services/api/presence/presence'; // 🚀 Ajusta esta ruta

@Component({
  selector: 'app-member-sidebar',
  standalone: true,
  imports: [CommonModule, UpperCasePipe, MemberProfilePopUp],
  templateUrl: './member-sidebar.html',
  styleUrl: './member-sidebar.css',
})
export class MemberSidebar implements OnChanges, OnDestroy {

  @Input() members: any[] = [];
  selectedMember: any | null = null;
  popupTop: number = 0;

  private presenceSubs: Subscription = new Subscription(); // 🚀 Guardamos las suscripciones

  constructor(
    private presenceService: PresenceService,
    private cdr: ChangeDetectorRef
  ) {}

  // 🚀 Se ejecuta cada vez que el padre (Server o Group DM) le manda la lista de miembros
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['members'] && this.members) {
      this.subscribeToPresence();
    }
  }

  ngOnDestroy(): void {
    this.presenceSubs.unsubscribe(); // Limpiamos al cerrar
  }

  private subscribeToPresence(): void {
    this.presenceSubs.unsubscribe();
    this.presenceSubs = new Subscription();

    this.members.forEach(member => {
      if (member.id || member.userId) { // Dependiendo de cómo se llame el ID en tu ServerResponse
        const idToWatch = member.id || member.userId;
        const statusSub = this.presenceService.watchUserStatus(idToWatch).subscribe({
          next: (rawStatus) => {
            const newStatus = rawStatus.replace(/['"]+/g, '').trim().toUpperCase();
            // 🚀 Actualizamos el objeto directamente. ¡El Pop-up lo notará automáticamente!
            member.status = newStatus;
            this.cdr.detectChanges();
          }
        });
        this.presenceSubs.add(statusSub);
      }
    });
  }

  toggleProfile(member: any, event: MouseEvent){
    if(this.selectedMember === member){
      this.selectedMember = null;
    } else {
      this.selectedMember = member;
      const target = event.currentTarget as HTMLElement;
      const rect = target.getBoundingClientRect();
      this.popupTop = rect.top - 20;
    }
  }

  closeProfile(){
    this.selectedMember = null;
  }

  // 🚀 Helper para los colores
  getStatusColor(status: string | undefined): string {
    switch (status) {
      case 'ONLINE': return 'bg-[#23a559]';
      case 'IDLE': return 'bg-[#f0b232]';
      case 'DO_NOT_DISTURB': return 'bg-[#f23f43]';
      case 'OFFLINE': 
      default: return 'bg-[#80848e]';
    }
  }
}