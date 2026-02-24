import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../../services/api/authservice/auth-service';

@Component({
  selector: 'app-user-panel',
  imports: [CommonModule],
  templateUrl: './user-panel.html',
  styleUrl: './user-panel.css',
  host: {
    'class': 'contents'
  }
})
export class UserPanel implements OnInit, OnDestroy {

  username: string = 'Usuario';
  private sub!: Subscription;

  constructor(
    private router: Router,
    private nickService: AuthService
  ){}

  ngOnInit(): void {
    this.sub = this.nickService.nickname$.subscribe(nickname => {
      this.username = nickname;
    });
  }

  ngOnDestroy() {
    if(this.sub) {
      this.sub.unsubscribe();
    }
  }


  logout(){
    console.log('Cerrando sesion...');
    localStorage.removeItem('token');
    this.nickService.clearSesion();
    this.router.navigate(['/login']);
  }

}

