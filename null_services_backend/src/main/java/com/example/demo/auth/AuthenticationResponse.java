package com.example.demo.auth;

import com.example.demo.user.UserStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) que representa la respuesta
 * del servidor tras un inicio de sesión exitoso.
 * <p>
 * Este objeto encapsula el token de seguridad (JWT) y los
 * datos básicos del usuario.
 * NOTA DE ARQUITECTURA: Enviar el 'nickname' y el 'email'
 * junto con el token es una
 * optimización. Evita que el frontend (Angular) tenga que hacer
 * una segunda petición HTTP
 * (ej. un GET /api/v1/users/me) inmediatamente después del
 * login solo para pintar
 * el nombre del usuario en la barra de navegación.
 * </p>
 */
@Getter
@Setter
@Builder
public class AuthenticationResponse {

    /**
     * El JSON Web Token (JWT) que el cliente debe incluir en el
     * header 'Authorization: Bearer ...'
     * en todas sus peticiones HTTP posteriores.
     * <p>
     * ¡Importante!: Este token contiene el 'userId' en sus
     * claims, el cual es extraído
     * por el frontend para inicializar las conexiones de WebSockets
     * en el módulo de chat.
     * </p>
     */
    private String token;

    /**
     * El alias o nombre de visualización del usuario.
     * Se envía al frontend para personalizar la
     * interfaz de usuario (UI) inmediatamente
     * después del login, sin necesidad de decodificar
     * el payload del JWT manualmente.
     */
    private String nickname;

    /**
     * El correo electrónico del usuario autenticado.
     * Sirve como identificador principal en el frontend para
     * mostrar en perfiles o menús de cuenta.
     */
    private String email;

    /**
     * El estado actual de presencia del usuario
     * (ONLINE, OFFLINE, AWAY, DO_NOT_DISTURB).
     * <p>
     * Se envía en el momento del login para que el frontend (Angular) pueda
     * inicializar el estado visual del usuario en la interfaz (ej. pintar el
     * indicador verde de "en línea") inmediatamente.
     * </p>
     */
    private UserStatus status;

}
