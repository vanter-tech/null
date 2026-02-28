package com.example.demo.user;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de la cuenta y preferencias del usuario autenticado.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints para la gestión del perfil del usuario")
public class UserController {

    private final UserService userService;

    /**
     * Cambia el estado de presencia del usuario actual.
     * <p>
     * NOTA DE ARQUITECTURA: Se utiliza @PatchMapping porque estamos modificando
     * un campo parcial del recurso (el estado del usuario) y no el objeto completo
     * (lo cual requeriría un @PutMapping).
     * </p>
     *
     * @param status El nuevo estado enviado como parámetro en la URL (ej. ?status=AWAY).
     * @param connectedUser Inyectado automáticamente por Spring Security.
     * @return 200 OK si el cambio fue exitoso.
     */
    @PatchMapping("/status")
    public ResponseEntity<Void> updateStatus(
            @RequestParam UserStatus status,
            Authentication connectedUser
    ) {
        userService.updatePresenceStatus(status, connectedUser);
        return ResponseEntity.ok().build();
    }
}