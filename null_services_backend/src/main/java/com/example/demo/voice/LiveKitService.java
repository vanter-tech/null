package com.example.demo.voice;

import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de la integración con LiveKit
 * para la gestión de canales de voz.
 * Genera tokens de acceso (JWT) que permiten
 * a los usuarios unirse a salas de
 * comunicación en tiempo real de forma segura y controlada.
 */
@Service
public final class LiveKitService {

    /**
     * Llave de API configurada para
     * el servidor de LiveKit (modo desarrollo).
     */
    private static final String API_KEY = "devkey";

    /**
     * Secreto de API utilizado para
     * firmar los tokens de acceso.
     */
    private static final String API_SECRET = "secret";

    /**
     * Genera un token de acceso dinámico para
     * que un usuario se una a un canal de voz.
     * El token incluye permisos específicos de
     * unión y el nombre de la sala.
     *
     * @param channelId El identificador único del canal de voz.
     * @param username El nombre o identificador del
     * usuario que solicita acceso.
     * @return El token JWT firmado necesario para
     * la conexión del cliente LiveKit.
     */
    public String generateVoiceToken(
            final String channelId,
            final String username) {

        // Creamos un token vacío con nuestras llaves maestras
        AccessToken token = new AccessToken(API_KEY, API_SECRET);

        // Establecemos la identidad del usuario en el sistema LiveKit
        token.setName(username);
        token.setIdentity(username);

        // Otorgamos permiso exclusivo para
        // unirse a la sala específica del canal
        token.addGrants(new RoomJoin(true), new RoomName("canal-" + channelId));

        // Firmamos el token y lo devolvemos en formato JWT
        return token.toJwt();
    }
}
