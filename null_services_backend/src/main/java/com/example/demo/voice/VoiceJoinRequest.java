package com.example.demo.voice;

import lombok.Data;

/**
 * Objeto de Transferencia de Datos (DTO)
 * utilizado para solicitar el acceso a un
 * canal de voz específico.
 * Contiene la información necesaria
 * para identificar al usuario y mostrar su
 * perfil a los demás integrantes de la sala.
 */
@Data
public class VoiceJoinRequest {

    /**
     * Identificador único del canal de voz al
     * que el usuario desea unirse.
     */
    private Long channelId;

    /**
     * Identificador único del usuario que realiza la petición.
     */
    private Long userId;

    /**
     * Nombre de usuario que se mostrará en la
     * interfaz de la sala de voz.
     */
    private String username;

    /**
     * URL de la imagen de perfil del usuario para
     * mostrar su avatar en la sala.
     */
    private String imageUrl;

}
