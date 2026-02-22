package com.example.demo.message;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "messages")
public class Message {

    @Id
    private String id;
    private String content;
    private Integer sendId;
    private Long conversationId;
    private LocalDateTime timestamp;
}
