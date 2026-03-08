import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-voice-control-panel',
  imports: [CommonModule],
  templateUrl: './voice-control-panel.html',
  styleUrl: './voice-control-panel.css',
})
export class VoiceControlPanel {

  @Input() channelName: string = '';
  @Input() serverName: string = '';
  showSmallPanel: boolean = false;
  // 📞 Un evento para avisarle al padre (server.ts) que queremos colgar
  @Output() onDisconnect = new EventEmitter<void>();

  disconnect() {
    this.onDisconnect.emit();
  }

}
