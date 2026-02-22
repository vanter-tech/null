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

    @PostMapping
    public ResponseEntity<Message> sendMessage(
            @RequestBody Message message
    ){
        return ResponseEntity.ok(
                messageService.saveMessage(message)
        );
    }

    @GetMapping("/chat/{conversationId}")
    public ResponseEntity<List<Message>> getChatHistory(
            @PathVariable Long conversationId
    ){
        return ResponseEntity.ok(messageService.findChatMessages(conversationId));
    }

}
