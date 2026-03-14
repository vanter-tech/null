import { Component, Input } from '@angular/core';
import { CommonModule, UpperCasePipe} from '@angular/common';

import { MemberProfilePopUp } from './components/member-profile-pop-up/member-profile-pop-up/member-profile-pop-up';

@Component({
  selector: 'app-member-sidebar',
  standalone: true,
  imports: [CommonModule, UpperCasePipe, MemberProfilePopUp],
  templateUrl: './member-sidebar.html',
  styleUrl: './member-sidebar.css',
})
export class MemberSidebar {

 @Input() members: any[] = []
  selectedMember: any | null = null;
  popupTop: number = 0; 

  
  toggleProfile(member: any, event: MouseEvent){
    if(this.selectedMember === member){
      this.selectedMember = null;
    } else {
      this.selectedMember = member;
      
      
      const target = event.currentTarget as HTMLElement;
      const rect = target.getBoundingClientRect();
    
      this.popupTop = rect.top - 1.5; 
    }
  }

  closeProfile(){
    this.selectedMember = null;
  }

}
