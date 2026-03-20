package com.example.demo.server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Objeto de Transferencia de Datos (DTO)
 * que representa a un miembro
 * de un servidor o canal. Se envía al
 * cliente con la información pública
 * necesaria para renderizar la lista de usuarios.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResponse {

    /**
     * Identificador único del usuario o miembro.
     * Útil para enlazar acciones como enviar
     * mensajes directos (MD).
     */
    private Integer id;

    /**
     * Nombre de usuario o apodo que se mostrará en la
     * interfaz gráfica.
     */
    private String username;

    /**
     * URL de la imagen de perfil o avatar del usuario.
     */
    private String imageUrl;

    /**
     * Estado actual de conexión del usuario
     * (por ejemplo: "ONLINE", "OFFLINE" o "BUSY").
     * Permite al frontend mostrar el indicador visual
     * correspondiente (punto verde, gris, etc.).
     */
    private String status;

}
