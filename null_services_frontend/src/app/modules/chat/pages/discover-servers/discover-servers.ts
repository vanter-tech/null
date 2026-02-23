import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ServerControllerService, ServerResponse } from '../../../../services/api';
import { DiscoverSidebar } from '../../components/discover-sidebar/discover-sidebar';

@Component({
  selector: 'app-discover-servers',
  standalone: true,
  imports: [CommonModule, DiscoverSidebar],
  templateUrl: './discover-servers.html',
  styleUrl: './discover-servers.css',
})
export class DiscoverServers implements OnInit {

  publicServers: ServerResponse[] = [];

  constructor(
    private serverService: ServerControllerService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadPublicServers();
  }

  loadPublicServers() {
    this.serverService.findAllServer().subscribe({
      next: (response) => {
        this.publicServers = response;
        console.log('Public servers loaded:', this.publicServers);
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading public servers:', error);
      }
    })
  }

}
