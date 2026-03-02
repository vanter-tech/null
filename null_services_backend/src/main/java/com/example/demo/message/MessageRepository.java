package com.example.demo.message;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message,String> {
    List<Message> findByConversationIdOrderByTimestampAsc(Long conversationId);

    List<Message> findByChannelIdOrderByTimestampAsc(Long channelId);
}
