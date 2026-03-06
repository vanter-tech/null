import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { FriendsControllerService } from '../../../../../../../services/api/api/friendsController.service';
import { RouterLink } from '@angular/router';


@Component({
  selector: 'app-friend-add',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './friend-add.html',
  styleUrl: './friend-add.css',
})
export class FriendAdd {

  targetUserId: string = '';

  statusMessage: string = '';
  isError: boolean = false;

  constructor(
    private friendService: FriendsControllerService,
    private cdr: ChangeDetectorRef
  ) {}

  sendFriendRequest() {
    const id = parseInt(this.targetUserId, 10);
    if (isNaN(id)) {
      this.showStatus('Please enter a valid user ID.', true);
      return;
    }

    this.friendService.requestFriends(id).subscribe({
      next:(response) =>{
        this.showStatus('Friend request sent successfully.', false);
        this.targetUserId = '';
      },
      error: (error) => {
        this.showStatus('Failed to send friend request: ', true);
        console.log(error);
      }
        
    });

  }

  private showStatus(message: string, isError: boolean): void {
    this.statusMessage = message;
    this.isError = isError;

    this.cdr.detectChanges();

    setTimeout(() => {
      this.statusMessage = '';
      this.cdr.detectChanges();
      }, 5000);
    }


}
