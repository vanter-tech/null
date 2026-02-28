package com.example.demo.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración principal del servidor WebSocket y el protocolo STOMP.
 * <p>
 * Esta clase habilita el motor de mensajería en tiempo real. Define los puntos de
 * conexión (endpoints) para que el frontend de Angular inicie el "handshake" y
 * configura las rutas por donde viajarán los mensajes de chat.
 * </p>
 */
@Configuration
@EnableWebSocketMessageBroker // Activa el broker de mensajería respaldado por WebSockets
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Variable inyectada desde application.yml (ej. http://localhost:4200)
    @Value("${application.cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * Registra el endpoint inicial (la puerta de entrada) del WebSocket.
     * <p>
     * Aquí es donde el cliente (Angular/SockJS) realiza la primera petición HTTP
     * para "actualizar" (upgrade) la conexión a un túnel WebSocket persistente.
     * </p>
     *
     * @param registry El registro de endpoints de STOMP.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // NOTA DE ARQUITECTURA: Actualmente acepta conexiones de cualquier origen ("*")
                // para facilitar el desarrollo. En producción, se debería cambiar a
                // setAllowedOrigins(allowedOrigins) para mayor seguridad.
                .setAllowedOriginPatterns("*");
    }

    /**
     * Configura el enrutador (Broker) de mensajes.
     * Define qué prefijos se usan para enviar mensajes al servidor y cuáles
     * se usan para que el servidor transmita mensajes a los clientes suscritos.
     *
     * @param registry El registro del broker de mensajes.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // 1. Prefijos de SALIDA (De Servidor a Cliente)
        // Los clientes (Angular) se suscribirán a rutas que empiecen por "/topic".
        // Ej: Servidor envía mensaje a "/topic/chat/1" y todos los suscritos lo reciben.
        registry.enableSimpleBroker("/topic");

        // 2. Prefijos de ENTRADA (De Cliente a Servidor)
        // Cuando Angular envíe un mensaje al servidor, la ruta debe empezar por "/app".
        // Ej: Angular envía a "/app/chat.sendMessage", y Spring lo rutea al @MessageMapping correspondiente.
        registry.setApplicationDestinationPrefixes("/app");
    }
}