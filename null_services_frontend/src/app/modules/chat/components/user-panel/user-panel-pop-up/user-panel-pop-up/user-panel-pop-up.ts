import { Component, EventEmitter, Output, Input } from '@angular/core';
import { AuthenticationResponse } from '../../../../../../services/api';
import { UserPopUpProfilePreview } from '../../../modals/user-pop-up-profile-preview/user-pop-up-profile-preview';
import { CommonModule } from '@angular/common';



@Component({
  selector: 'app-user-panel-pop-up',
  imports: [CommonModule, UserPopUpProfilePreview],
  templateUrl: './user-panel-pop-up.html',
  styleUrl: './user-panel-pop-up.css',
})
export class UserPanelPopUp {
  @Input() username: String = 'Usuario'
  @Input() currentStatus!: AuthenticationResponse.StatusEnum;
  @Output() statusSelected = new EventEmitter<AuthenticationResponse.StatusEnum>();
  @Output() closeP = new EventEmitter<void>();


  public statusEnum = AuthenticationResponse.StatusEnum

  showSubMenu: boolean = false;
  showFullModal: boolean = false;
  isPanelVisible: boolean = true;

  toggleProfilePreview(event: Event): void{
    event.stopPropagation();

    this.showFullModal = true;
    this.showSubMenu = false;
    this.isPanelVisible = false;

  }

  closeEverything(): void{
    this.showFullModal = false
    this.closeP.emit();
  }


  toggleSubMenu(event: Event){
    event.stopPropagation();
    this.showSubMenu = !this.showSubMenu
  }

  selectStatus(status: AuthenticationResponse.StatusEnum, event: Event){
    event?.stopPropagation();
    this.statusSelected.emit(status)
    this.showSubMenu = false;
  }

}
