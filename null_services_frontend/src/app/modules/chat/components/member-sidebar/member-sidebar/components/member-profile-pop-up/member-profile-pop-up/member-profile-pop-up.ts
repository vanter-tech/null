import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-member-profile-pop-up',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './member-profile-pop-up.html',
  styleUrl: './member-profile-pop-up.css',
})
export class MemberProfilePopUp {

@Input({required: true}) member: any;
@Output() close = new EventEmitter<void>();
@Input() topPosition: number = 0;

}
