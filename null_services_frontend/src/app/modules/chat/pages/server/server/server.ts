import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';

import { ServerControllerService } from '../../../../../services/api';
import { ServerResponse } from '../../../../../services/api';
import { ChannelResponse } from '../../../../../services/api';

@Component({
  selector: 'app-server',
  imports: [CommonModule, RouterModule],
  templateUrl: './server.html',
  styleUrl: './server.css',
})
export class Server implements OnInit{

  currentServerId: number | null = null;
  serverData: ServerResponse | null = null;
  activeChannelId: number | null = null;
  isMembersListOpen: boolean = true;

  isServerMenuOpen: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private serverService: ServerControllerService,
    private cdr: ChangeDetectorRef
  ){}

  ngOnInit(): void {
      this.route.paramMap.subscribe(param => {
        const idStr = param.get('serverId');
        if(idStr){
          this.currentServerId = Number(idStr);
          this.loadServerData(this.currentServerId);
        }
      })
  }

  loadServerData(id: number){

    this.serverData = null;
    this.isServerMenuOpen = false;

    this.serverService.findServerById(id).subscribe({
      next: (data: ServerResponse) => {
        console.log('Server load successfully', data);
        this.serverData = data;

        if(data.channels && data.channels.length > 0){
          this.activeChannelId = data.channels[0].id || null;
        }else{
          this.activeChannelId = null;
        }

        this.cdr.detectChanges();
      },
      error: (err) => {
        console.log('Failed to load server', err);
        
      }
    })
  }


  leaveServer(): void{
    if(!this.currentServerId) return

    this.serverService.leaveServer(this.currentServerId).subscribe({
      next: () => {
        console.log('You has leave the server');
        window.dispatchEvent(new CustomEvent('server-joined'))
        this.router.navigate(['/home'])
      },
      error: (err) => {
        console.log('failed leaving the server', err);
        
      }
    })

  }


  toggleServerMenu(): void {
    this.isServerMenuOpen = !this.isServerMenuOpen;
  }

  selectChannel(channelId: number | undefined): void{
    if(channelId !== undefined){
      this.activeChannelId = channelId
    }
  }

  toggleMembersList(): void {
    this.isMembersListOpen = !this.isMembersListOpen;
  }

  getActiveChannelName(){
    if(!this.serverData?.channels || !this.activeChannelId) return 'general'
    const channel = this.serverData.channels.find(c => c.id === this.activeChannelId);
    return channel ? channel.name || 'general' : 'general';
  }

}
