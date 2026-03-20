package com.example.demo.voice;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * Controlador de mensajería encargado de gestionar
 * la presencia en los canales de voz.
 * Utiliza el protocolo STOMP sobre WebSockets
 * para recibir eventos de unión,
 * salida y sincronización de los participantes en tiempo real.
 */
@Controller
@RequiredArgsConstructor
public class VoicePresenceController {

    /**
     * Servicio encargado de mantener el
     * estado de las salas de voz y emitir
     * las notificaciones a los suscriptores.
     */
    private final VoicePresenceService presenceService;

    /**
     * Gestiona la entrada de un usuario a
     * un canal de voz específico.
     * Transforma la petición en un objeto
     * participante y actualiza el estado global.
     *
     * @param serverId El identificador del servidor
     * donde se encuentra el canal.
     * @param request Objeto con los datos del usuario
     * que desea unirse.
     */
    @MessageMapping("/server/{serverId}/voice/join")
    public void joinVoice(
            final @DestinationVariable Long serverId,
            final VoiceJoinRequest request) {

        VoiceParticipant participant = new VoiceParticipant(
                request.getUserId(),
                request.getUsername(),
                request.getImageUrl()
        );

        presenceService.joinRoom(serverId, request.getChannelId(), participant);
    }

    /**
     * Gestiona la salida de un usuario de un canal de voz.
     * Informa al servicio de presencia para
     * que elimine al usuario de la lista activa.
     *
     * @param serverId El identificador del servidor.
     * @param request Objeto que contiene el ID del
     * canal y del usuario que sale.
     */
    @MessageMapping("/server/{serverId}/voice/leave")
    public void leaveVoice(
            final @DestinationVariable Long serverId,
            final VoiceJoinRequest request) {

        presenceService.leaveRoom(
                serverId,
                request.getChannelId(),
                request.getUserId());
    }

    /**
     * Sincroniza el estado actual de todas
     * las salas de voz de un servidor.
     * Permite que los usuarios que recién se
     * conectan obtengan la lista completa
     * de quién está en cada canal.
     *
     * @param serverId El identificador del
     * servidor a sincronizar.
     */
    @MessageMapping("/server/{serverId}/voice/sync")
    public void syncVoice(
            final @DestinationVariable Long serverId) {
        presenceService.broadcastState(serverId);
    }
}
