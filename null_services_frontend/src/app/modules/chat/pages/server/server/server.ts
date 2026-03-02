import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';

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

  constructor(
    private route: ActivatedRoute,
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
