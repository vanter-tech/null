package com.example.demo.websocket;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.example.demo.user.UserStatus;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Escuchador de eventos de WebSocket.
 * Detecta cuándo un usuario se
 * conecta o desconecta del broker STOMP
 * para actualizar su estado de presencia
 * (Online/Offline) en la base de datos
 * y notificar a los demás clientes en tiempo real.
 */
@Component
@RequiredArgsConstructor
public final class WebSocketEventListener {

    /** Repositorio para actualizar
     * el estado del usuario en la BD. */
    private final UserRepository userRepository;

    /** Plantilla para enviar mensajes
     * STOMP a los tópicos suscritos. */
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Captura el evento de desconexión de un socket.
     * Actualiza el estado a OFFLINE y emite un mensaje STOMP.
     * Se usa Transactional para asegurar
     * la actualización fuera del hilo HTTP.
     *
     * @param event El evento de desconexión disparado por Spring.
     */
    @EventListener
    @Transactional
    public void handleWebSocketDisconnectListener(
            final SessionDisconnectEvent event) {

        System.out.println("🔴 1. Evento de desconexión disparado...");
        Principal principal = event.getUser();

        if (principal == null) {
            System.out.println("❌ 2. Principal NULL. Sigue anónimo.");
            return;
        }

        System.out.println("👤 2. Usuario: " + principal.getName());

        if (principal instanceof Authentication auth) {
            try {
                User userPrincipal = (User) auth.getPrincipal();
                Integer userId = userPrincipal.getId();

                System.out.println("🔍 3. Buscando ID: " + userId);

                userRepository.findById(userId).ifPresent(user -> {

                    System.out.println("💾 4. Guardando OFFLINE: "
                            + user.getNickName());
                    user.setStatus(UserStatus.OFFLINE);
                    userRepository.save(user);

                    String dest = "/topic/user/status/" + user.getId();
                    System.out.println("📣 5. STOMP a: " + dest);
                    messagingTemplate.convertAndSend(dest, "OFFLINE");

                    System.out.println("✅ 6. Desconexión exitosa.");
                });

            } catch (ClassCastException e) {
                System.out.println("❌ ERR: Principal no es User. Es: "
                        + auth.getPrincipal().getClass().getName());
            } catch (Exception e) {
                System.out.println("❌ ERR INESPERADO: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Captura el evento de conexión de un nuevo socket.
     * Actualiza el estado a ONLINE y emite la notificación a la red.
     *
     * @param event El evento de conexión disparado por Spring.
     */
    @EventListener
    @Transactional
    public void handleWebSocketConnectListener(
            final SessionConnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor
                .wrap(event.getMessage());
        Principal principal = accessor.getUser();

        if (principal instanceof Authentication auth) {
            User userPrincipal = (User) auth.getPrincipal();
            Integer userId = userPrincipal.getId();

            userRepository.findById(userId).ifPresent(user -> {
                user.setStatus(UserStatus.ONLINE);
                userRepository.save(user);

                String dest = "/topic/user/status/" + user.getId();
                messagingTemplate.convertAndSend(dest, "ONLINE");

                System.out.println("🟢 Usuario ONLINE: "
                        + user.getNickName());
            });
        }
    }
}
