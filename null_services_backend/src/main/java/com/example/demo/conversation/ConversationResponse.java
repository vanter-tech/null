package com.example.demo.conversation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Objeto de transferencia de datos (DTO) que
 * representa la respuesta de una conversación.
 * Contiene la información pública y básica que se envía
 * al cliente (frontend).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationResponse {

    /**
     * Identificador único de la conversación.
     */
    private Long id;
    /**
     * Nombre del otro usuario que participa en
     * la conversación.
     * Útil para mostrar el título del chat en
     * la interfaz de usuario.
     */
    private String otherUserName;
}
