import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Main } from './pages/main/main';
import { authGuard } from '../../services/api/guard/authguard';
import { ChatRoom } from './pages/chat-room/chat-room';
import { DiscoverServers } from './pages/discover-servers/discover-servers';

const routes: Routes = [

  {
    path: '',
    component: Main,
    canActivate: [authGuard],
    children: [
      {
        path: 'server/:serverId/channel/:channelId',
        component: ChatRoom,
        canActivate: [authGuard]
      },
      {
        path: 'discover',
        component: DiscoverServers,
        canActivate: [authGuard]
      }
    ]
  }

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ChatRoutingModule { }
