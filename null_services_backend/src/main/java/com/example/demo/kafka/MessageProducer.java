package com.example.demo.kafka;

import com.example.demo.message.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducer {

    private final KafkaTemplate<String, Message> kafkaTemplate;

    private static final String TOPIC = "chat-messages";

    public void sendMessage(Message message){
        log.info("Enviando mensaje a Kafka para la sala {}: {}", message.getConversationId(), message.getContent());
        kafkaTemplate.send(TOPIC, String.valueOf(message.getConversationId()), message);
    }

}
