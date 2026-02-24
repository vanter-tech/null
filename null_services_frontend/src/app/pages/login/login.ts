import { Component } from '@angular/core';
import { AuthenticationRequest } from '../../services/api';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthenticationService } from '../../services/api';
import { Token } from '../../services/api/token/token';
import { AuthService } from '../../services/api/authservice/auth-service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {

  authRequest: AuthenticationRequest = {
    username: '',
    password: ''
  };

  errorMessage: Array<string> = [];

  constructor(
    private authService: AuthenticationService,
    private router: Router,
    private tokenService: Token,
    private nickService: AuthService
  ) {
    
  }

  login() {
    this.errorMessage = [];
    this.authService.authenticate(this.authRequest).subscribe({
      next: (response) => {
        this.tokenService.token = response.token as string;

        if(response.nickname) {
          this.nickService.setNickname(response.nickname);
        }

        console.log('Inicio de Sesion Exitoso', response);
        this.router.navigate(['/chat']);

      },
      error: (error) => {

        console.error('Error al iniciar sesión', error);

        if(error.error.ValidationErrors) {
          this.errorMessage = error.error.ValidationErrors
        }else{
          this.errorMessage.push(error.error.message);
        }

      }
    });
  }

  register() {
    this.router.navigate(['/register']);
  }

}
