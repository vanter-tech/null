import { CommonModule } from '@angular/common';
import { Component, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-user-pop-up-profile-preview',
  imports: [CommonModule],
  templateUrl: './user-pop-up-profile-preview.html',
  styleUrl: './user-pop-up-profile-preview.css',
})
export class UserPopUpProfilePreview {

  @Output() closePopUp = new EventEmitter<void>();

    closeModal(): void{
    this.closePopUp.emit();
  }

}
