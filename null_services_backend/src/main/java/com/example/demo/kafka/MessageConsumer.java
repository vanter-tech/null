package com.example.demo.kafka;

import com.example.demo.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageConsumer {
    @KafkaListener(topics = "chat-messages", groupId = "null-group")
    public void consume(Message message) {
        log.info("Received message from kafka '{}' de la sala: {}",
                message.getContent(),
                message.getConversationId());
    }
}
