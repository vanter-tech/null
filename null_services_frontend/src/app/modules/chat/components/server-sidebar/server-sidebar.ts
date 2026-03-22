import { Component, ChangeDetectorRef, OnInit, inject } from '@angular/core';
import { CommonModule} from '@angular/common';
import { RouterModule } from '@angular/router';
import { ServerControllerService } from '../../../../services/api';
import { ServerResponse } from '../../../../services/api';
import { ServerRequest } from '../../../../services/api';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-server-sidebar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './server-sidebar.html',
  styleUrl: './server-sidebar.css',
})
export class ServerSidebar implements OnInit {

  servers: ServerResponse[] = [];

  isModalOpen = false;
  serverNameInput = '';
  serverImageInput = '';

  private serverService = inject(ServerControllerService)
  private cdr = inject(ChangeDetectorRef)

  ngOnInit() {
    this.loadServers();
    window.addEventListener('server-joined', () => {
    this.loadServers();
  });
  }

  loadServers() {
    this.serverService.findMyServers().subscribe({
      next: (response) => {
        this.servers = response;
        console.log('Servers loaded:', this.servers);
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading servers:', error);
      }
    })
  }

  openModal() {
    this.isModalOpen = true;
  }

  closeModal() {
    this.isModalOpen = false;
    this.serverNameInput = '';
    this.serverImageInput = '';
  }

  createServer() {
    if(!this.serverNameInput.trim()) return;

    const newServer: ServerRequest = {
      name: this.serverNameInput, 
      imageUrl: this.serverImageInput || undefined
    };

    this.serverService.saveServer(newServer).subscribe({
      next: (response) => {
        console.log('Server created:', response);
        this.loadServers();
        this.closeModal();
      },
      error: (error) => {
        console.error('Error creating server:', error);
      }
    })
  }

}
