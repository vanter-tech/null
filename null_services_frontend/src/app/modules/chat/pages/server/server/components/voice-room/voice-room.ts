import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-voice-room',
  imports: [CommonModule],
  templateUrl: './voice-room.html',
  styleUrl: './voice-room.css',
})
export class VoiceRoom {

  // 👥 Recibe la lista de los que están en la llamada
  @Input({ required: true }) participants: any[] = []; 
  // 🏷️ Recibe el nombre del canal para mostrarlo arriba
  @Input({ required: true }) channelName: string = '';

}
