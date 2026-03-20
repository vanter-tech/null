package com.example.demo.user;

/**
 * Enum que define los posibles estados de
 * presencia de un usuario en la plataforma.
 * Estos estados determinan cómo los demás
 * usuarios ven la disponibilidad del usuario
 * en la lista de amigos y en los servidores.

 */
public enum UserStatus {

    /**
     * El usuario está conectado y activo.
     */
    ONLINE,

    /**
     * El usuario no está conectado al WebSocket.
     */
    OFFLINE,

    /**
     * El usuario está conectado, pero inactivo o lejos del teclado.
     */
    AWAY,

    /**
     * El usuario está conectado, pero no desea recibir
     * notificaciones de sonido/popups.
     */
    DO_NOT_DISTURB
}
