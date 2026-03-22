import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-delete-server-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './delete-server-modal.html'
})
export class DeleteServerModal {
  // Recibimos el nombre del servidor para mostrarlo y validarlo
  @Input({ required: true }) serverName!: string;

  // Emitimos eventos al componente padre
  @Output() closePopUp = new EventEmitter<void>();
  @Output() confirmDelete = new EventEmitter<void>();

  // Lo que el usuario escribe en el input
  confirmInput = '';

  closeModal(): void {
    this.closePopUp.emit();
  }

  // Comprueba si el texto ingresado coincide exactamente con el nombre
  get isMatch(): boolean {
    return this.confirmInput === this.serverName;
  }

  onDeleteClick(): void {
    if (this.isMatch) {
      this.confirmDelete.emit();
    }
  }
}