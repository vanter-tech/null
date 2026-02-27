package com.example.demo.conversation;


import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ConversationResponse createOrGetConversation(
            Integer targetUserId,
            Authentication connectedUser
    ){

        User currentUser = (User) connectedUser.getPrincipal();
        if(currentUser.getId().equals(targetUserId)){
            throw new RuntimeException("You can't create a conversation with yourself");
        }

        Optional<Conversation> existingConversation = conversationRepository
                .findConversationByUsers(currentUser.getId(), targetUserId);

        if(existingConversation.isPresent()){
            return mapToResponse(existingConversation.get(), currentUser);
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Conversation newConversation = Conversation.builder()
                .participants(Set.of(currentUser,targetUser))
                .build();

        Conversation savedConversation = conversationRepository.save(newConversation);

        return mapToResponse(savedConversation, currentUser);
    }

    private ConversationResponse mapToResponse(Conversation conv, User currentUser) {
        User otherUser = conv.getParticipants().stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(currentUser);

        return ConversationResponse.builder()
                .id(conv.getId())
                .otherUserName(otherUser.getNickName())
                .build();
    }

    public List<ConversationResponse> getUserConversation(Authentication connectedUser) {

        User user = (User) connectedUser.getPrincipal();

        List<Conversation> conversations = conversationRepository.findAllByUserId(user.getId());

        return conversations.stream()
                .map(conversation -> {
                    User friend = conversation.getParticipants().stream()
                            .filter(p -> !p.getId().equals(user.getId()))
                            .findFirst()
                            .orElse(user);

                    return ConversationResponse.builder()
                            .id(conversation.getId())
                            .otherUserName(friend.getNickName() != null ? friend.getNickName() : friend.getFullname())
                            .build();
                })
                .toList();

    }
}
