import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-user-panel',
  imports: [CommonModule],
  templateUrl: './user-panel.html',
  styleUrl: './user-panel.css',
  host: {
    'class': 'block w-full h-full'
  }
})
export class UserPanel {

  constructor(
    private router: Router
  ){}

  logout(){
    console.log('Cerrando sesion...');
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }

}

