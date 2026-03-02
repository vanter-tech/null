package com.example.demo.message;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "messages")
// 🚀 Envolvemos los índices para tener uno para MDs y otro para Canales
@CompoundIndexes({
        @CompoundIndex(name = "conv_time_idx", def = "{'conversationId': 1, 'timestamp': 1}"),
        @CompoundIndex(name = "channel_time_idx", def = "{'channelId': 1, 'timestamp': 1}")
})
public class Message {

    @Id
    private String id;
    private String content;
    private Integer sendId; // Quien lo envía

    private Long conversationId; // 👈 Si tiene este, es un Mensaje Directo

    // 🚀 NUEVO: El ID del canal del servidor
    private Long channelId;      // 👈 Si tiene este, es un Mensaje de Servidor

    private LocalDateTime timestamp;
}