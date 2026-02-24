import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ServerSidebar } from "../../components/server-sidebar/server-sidebar";
import { UserPanel } from '../../components/user-panel/user-panel';

@Component({
  selector: 'app-main',
  imports: [RouterOutlet, ServerSidebar, UserPanel],
  templateUrl: './main.html',
  styleUrl: './main.css',
})
export class Main {

}
