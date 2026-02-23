import { Component } from '@angular/core';
import { CommonModule} from '@angular/common';

@Component({
  selector: 'app-discover-sidebar',
  imports: [CommonModule],
  templateUrl: './discover-sidebar.html',
  styleUrl: './discover-sidebar.css',
})
export class DiscoverSidebar {

  activeTab: string = 'server';

  setActiveTab(tab: string) {
    this.activeTab = tab;
  }


}
