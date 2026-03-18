package com.example.demo.kafka;

import com.example.demo.message.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio consumidor de Kafka encargado de
 * escuchar los mensajes de chat entrantes.
 * Su función principal es enrutar en tiempo real
 * los mensajes recibidos hacia los
 * clientes conectados a través de WebSockets,
 * diferenciando entre chats privados y canales.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConsumer {

    /**
     * Plantilla de mensajería de Spring
     * utilizada para enviar los mensajes a través
     * del broker de WebSockets hacia destinos
     * específicos (tópicos de STOMP).
     */
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Escucha y procesa los mensajes entrantes
     * desde el tópico configurado en Kafka.
     * Evalúa los identificadores del mensaje para
     * enrutarlo dinámicamente al canal
     * de servidor correspondiente o al chat privado adecuado.
     *
     * @param message El objeto de mensaje recuperado
     * de Kafka que contiene el texto
     * y los identificadores de ruta (channelId o conversationId).
     */
    @KafkaListener(topics = "chat-messages", groupId = "null-group")
    public void consume(
            final Message message) {

        // VERIFICACIÓN DE ENRUTAMIENTO (ROUTING)
        // Revisamos si el mensaje tiene un
        // ID de canal (Es para un Servidor)
        if (message.getChannelId() != null) {
            log.info("Recibido de Kafka (Canal): '{}' para channelId: {}",
                    message.getContent(), message.getChannelId());

            // 💡 IMPORTANTE: Enviamos a la ruta de canales
            String destination = "/topic/channel/" + message.getChannelId();
            messagingTemplate.convertAndSend(destination, message);
        } else if (message.getConversationId() != null) {
            log.info(
                    "Recibido de Kafka (Chat Privado): '{}' "
                            +
                            "para conversationId: {}",
                    message.getContent(), message.getConversationId());

            // IMPORTANTE: Enviamos a la ruta de chat privado
            String destination = "/topic/chat/" + message.getConversationId();
            messagingTemplate.convertAndSend(destination, message);
            } else {
            log.warn("Mensaje huérfano recibido en Kafka: "
                    +
                    "no tiene channelId ni conversationId");
        }
    }
}
