package com.example.demo.conversation;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/{targetUserId}")
    public ResponseEntity<ConversationResponse> createConversation(
            @PathVariable Integer targetUserId,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(
                conversationService.createOrGetConversation(
                        targetUserId,
                        connectedUser
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getConversation(
            Authentication connectedUser
    ){
        return ResponseEntity.ok(conversationService.getUserConversation(connectedUser));
    }

}
