import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FriendsControllerService } from '../../../../../../../services/api/api/friendsController.service';
import { FriendRequestDTO } from '../../../../../../../services/api';

@Component({
  selector: 'app-friends-pending',
  imports: [CommonModule],
  templateUrl: './friends-pending.html',
  styleUrl: './friends-pending.css',
})
export class FriendsPending implements OnInit {

  pendingRequests: FriendRequestDTO[] = [];

  constructor(
    private friendsService: FriendsControllerService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
      this.fetchPendingRequests();
  }

  fetchPendingRequests(): void {

    this.friendsService.getPendingRequests().subscribe({
      next: (data) => {
        this.pendingRequests = data;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error fetching pending friend requests:', error);
      }
    });

  }

  acceptRequest(friendshipId: number): void {

    if(!friendshipId) return;

    this.friendsService.acceptFriendRequest(friendshipId!).subscribe({
      next: () => {
        this.pendingRequests = this.pendingRequests.filter(req => req.friendshipId !== friendshipId);
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error accepting friend request:', error);
      }
    });

  }
}
