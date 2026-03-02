package com.example.demo.message;

import com.example.demo.conversation.ConversationRepository;
import com.example.demo.kafka.MessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MessageProducer messageProducer;

    public Message saveMessage(Message message) {

        // 🚦 Guardia de tráfico: ¿Es un MD o es de un Canal?
        if (message.getConversationId() != null) {
            // Lógica original de MD
            if(!conversationRepository.existsById(message.getConversationId())){
                throw new RuntimeException("Error! This conversation does not exist!");
            }
        } else if (message.getChannelId() != null) {
            // Lógica nueva de Canal (Aquí en el futuro podríamos validar si el canal existe con un ChannelRepository)
        } else {
            throw new IllegalArgumentException("El mensaje debe pertenecer a una conversación o a un canal.");
        }

        message.setTimestamp(LocalDateTime.now());
        Message savedMessage = messageRepository.save(message);

        // ¡Tu Kafka sigue funcionando para ambos!
        messageProducer.sendMessage(savedMessage);

        return savedMessage;
    }

    // El que ya tenías
    public List<Message> findChatMessages(Long conversationId){
        return messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    // 🚀 NUEVO: Buscar mensajes del canal
    public List<Message> findChannelMessages(Long channelId){
        return messageRepository.findByChannelIdOrderByTimestampAsc(channelId);
    }
}