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

    public Message saveMessage(
            Message message
    ){
        if(!conversationRepository.existsById(message.getConversationId())){
            throw new RuntimeException("Error! This conversation does not exist!");
        }
        message.setTimestamp(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        messageProducer.sendMessage(savedMessage);

        return messageRepository.save(message);
    }

    public List<Message> findChatMessages(Long conversationId){
        return messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }
}
