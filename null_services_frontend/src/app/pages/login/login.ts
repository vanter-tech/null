import { Component, inject } from '@angular/core';
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

  errorMessage: string[] = [];
  private authService = inject(AuthenticationService)
  private router = inject(Router)
  private tokenService = inject(Token)
  private nickService = inject(AuthService)

  login() {
    this.errorMessage = [];
    this.authService.authenticate(this.authRequest).subscribe({
      next: (response) => {
        // 1. Guardamos el token
        this.tokenService.token = response.token as string;

        // 🚀 2. LA MAGIA: Ya no usamos setNickname ni guardamos el status a mano.
        // Le pasamos toda la respuesta al servicio central para que él se encargue de todo.
        this.nickService.setCurrentUser(response);

        console.log('Inicio de Sesion Exitoso', response);
        this.router.navigate(['/home']);

      },
      error: (error) => {
        console.error('Error al iniciar sesión', error);

        if(error.error.ValidationErrors) {
          this.errorMessage = error.error.ValidationErrors
        } else {
          this.errorMessage.push(error.error.message);
        }

      }
    });
  }

  register() {
    this.router.navigate(['/register']);
  }

}