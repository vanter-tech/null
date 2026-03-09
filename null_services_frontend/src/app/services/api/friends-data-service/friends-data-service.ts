import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { FriendResponseDTO } from '../model/friendResponseDTO';
import { FriendsControllerService } from '../api/friendsController.service';
@Injectable({
  providedIn: 'root'
})
export class FriendsDataService {

  private allFriendsSubject = new BehaviorSubject<FriendResponseDTO[]>([]);
  public allFriends$: Observable<FriendResponseDTO[]> = this.allFriendsSubject.asObservable();
  
  private hasLoaded = false; // 🚀 Bandera mágica de caché

  constructor(private friendsControllerService: FriendsControllerService) { }

  /**
   * Carga los amigos. Si ya están cargados en memoria, no repite la llamada HTTP
   * a menos que le pases forceReload = true.
   */
  loadAllFriends(forceReload: boolean = false): void {
    if (this.hasLoaded && !forceReload) {
      return; // 🛑 Ya los tenemos, detenemos la llamada a la API
    }

    this.friendsControllerService.getMyFriends().subscribe({
      next: (friends) => {
        // Ordenamos alfabéticamente para que siempre se vea bien
        const sortedFriends = friends.sort((a, b) => 
          (a.name || '').localeCompare(b.name || '')
        );
        this.allFriendsSubject.next(sortedFriends);
        this.hasLoaded = true; // ✅ Marcamos como cargado
      },
      error: (err) => console.error('Error al cargar la lista global de amigos:', err)
    });
  }

  // Permite forzar una recarga (útil para cuando agregas un amigo nuevo)
  forceReload(): void {
    this.loadAllFriends(true);
  }
}