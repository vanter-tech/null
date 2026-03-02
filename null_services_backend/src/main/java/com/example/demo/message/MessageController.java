package com.example.demo.message;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // Guarda mensajes (ya sea de MD o de Canal, el Service lo decide)
    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody Message message){
        return ResponseEntity.ok(messageService.saveMessage(message));
    }

    // El que ya tenías para MD
    @GetMapping("/chat/{conversationId}")
    public ResponseEntity<List<Message>> getChatHistory(@PathVariable Long conversationId){
        return ResponseEntity.ok(messageService.findChatMessages(conversationId));
    }

    // 🚀 NUEVO: El que llamará Angular al entrar a un canal (ej. localhost:8080/messages/channel/1)
    @GetMapping("/channel/{channelId}")
    public ResponseEntity<List<Message>> getChannelHistory(@PathVariable Long channelId){
        return ResponseEntity.ok(messageService.findChannelMessages(channelId));
    }

}