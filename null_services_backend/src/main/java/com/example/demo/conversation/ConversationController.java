package com.example.demo.conversation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * ENDPOINT: POST /conversations/{targetUserId}
     * Propósito: Crea o recupera un chat de 1 vs 1.
     * * @param targetUserId El ID del amigo con el que quieres hablar (viene en la URL).
     * @param connectedUser El usuario que está haciendo la petición (obtenido del token JWT).
     * @return Los datos básicos de la conversación (ConversationResponse).
     */
    @PostMapping("/{targetUserId}")
    public ResponseEntity<ConversationResponse> createConversation(
            @PathVariable Integer targetUserId,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(
                conversationService.createOrGetConversation(targetUserId, connectedUser)
        );
    }

    /**
     * ENDPOINT: POST /conversations/group
     * Propósito: Crea un chat grupal nuevo con múltiples personas.
     * * @param targetUserIds Una lista de IDs de amigos enviada en el body (ej: [2, 5, 8]).
     * @param connectedUser El usuario que está creando el grupo (obtenido del token JWT).
     * @return Los datos de la conversación grupal creada.
     */
    @PostMapping("/group")
    public ResponseEntity<ConversationResponse> createGroupConversation(
            @RequestBody List<Integer> targetUserIds,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(
                conversationService.createGroupConversation(targetUserIds, connectedUser)
        );
    }

    /**
     * ENDPOINT: GET /conversations
     * Propósito: Obtiene la lista de todos los chats (1v1 o grupales) a los que pertenece el usuario.
     * Esto alimenta tu barra lateral izquierda en Angular (dm-sidebar).
     * * @param connectedUser El usuario autenticado actual.
     * @return Una lista con todas sus conversaciones.
     */
    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getConversation(
            Authentication connectedUser
    ){
        return ResponseEntity.ok(conversationService.getUserConversation(connectedUser));
    }
}