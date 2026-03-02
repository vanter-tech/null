import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';

@Component({
  selector: 'app-server',
  imports: [CommonModule, RouterModule],
  templateUrl: './server.html',
  styleUrl: './server.css',
})
export class Server implements OnInit{

  currentServerId: String | null = null;
  serverName: string = 'Servidor de Prueba'

  textChannels = [
    {id: '1', name: 'general'},
    {id: '2', name: 'anuncios'},
    {id: '3', name: 'off-topics'}
  ];

  activeChannelId: string = '1';

  isMembersListOpen: boolean = true;

  constructor(
    private route: ActivatedRoute
  ){}

  ngOnInit(): void {
      this.route.paramMap.subscribe(param => {
        this.currentServerId = param.get('serverId')
      })
  }

  selectChannel(channelId: string): void{
    this.activeChannelId = channelId
  }

  toggleMembersList(): void {
    this.isMembersListOpen = !this.isMembersListOpen;
  }

}
