package com.example.demo.conversation;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    /**
     * LÓGICA: CHAT 1 VS 1
     * Busca si ya existe un chat entre tú y tu amigo. Si existe, lo devuelve.
     * Si no existe, crea uno nuevo.
     */
    public ConversationResponse createOrGetConversation(
            Integer targetUserId,
            Authentication connectedUser
    ){
        // 1. Obtenemos quién eres tú desde el token de seguridad
        User currentUser = (User) connectedUser.getPrincipal();

        // 2. Regla de negocio: Evitar crear un chat contigo mismo
        if(currentUser.getId().equals(targetUserId)){
            throw new RuntimeException("No puedes crear una conversación contigo mismo");
        }

        // 3. Buscamos en la BD si ya tienen un chat previo
        Optional<Conversation> existingConversation = conversationRepository
                .findConversationByUsers(currentUser.getId(), targetUserId);

        // 4. Si ya existe, simplemente devolvemos ese chat (no creamos duplicados)
        if(existingConversation.isPresent()){
            return mapToResponse(existingConversation.get(), currentUser);
        }

        // 5. Si no existe, buscamos al amigo en la BD para asegurarnos de que sea real
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 6. Creamos la nueva conversación y metemos a ambos en el Set (conjunto)
        Conversation newConversation = Conversation.builder()
                .participants(Set.of(currentUser, targetUser))
                .build();

        // 7. Guardamos en BD y devolvemos la respuesta mapeada
        Conversation savedConversation = conversationRepository.save(newConversation);
        return mapToResponse(savedConversation, currentUser);
    }

    /**
     * LÓGICA: CHAT GRUPAL
     * Recibe una lista de IDs, los busca a todos, te incluye a ti en la lista,
     * y crea una sola conversación que los vincula a todos.
     */
    public ConversationResponse createGroupConversation(
            List<Integer> targetUserIds,
            Authentication connectedUser
    ) {
        // 1. Obtenemos tu usuario
        User currentUser = (User) connectedUser.getPrincipal();

        // 2. Buscamos a todos los amigos enviados desde el modal de Angular
        List<User> targetUsers = userRepository.findAllById(targetUserIds);

        // 3. Validación de seguridad
        if (targetUsers.isEmpty()) {
            throw new RuntimeException("No se encontraron usuarios válidos para el grupo");
        }

        // 4. Creamos un Set vacío y agregamos a todos los amigos
        Set<User> participants = new HashSet<>(targetUsers);

        // 5. Te agregamos a ti (el creador) al grupo
        participants.add(currentUser);

        // 6. Construimos la entidad de la conversación con todos los participantes
        Conversation newConversation = Conversation.builder()
                .participants(participants)
                .build();

        // 7. Guardamos en BD. Hibernate creará los registros en la tabla intermedia automáticamente.
        Conversation savedConversation = conversationRepository.save(newConversation);

        return mapToResponse(savedConversation, currentUser);
    }

    /**
     * LÓGICA: OCULTAR CONVERSACIÓN
     * Oculta un chat (1v1 o grupal) de la vista del usuario actual,
     * añadiéndolo a la "lista negra" visual (hiddenBy).
     */
    public void hideConversation(Long conversationId, Authentication connectedUser) {
        // 1. Obtenemos tu usuario desde el token de seguridad
        User currentUser = (User) connectedUser.getPrincipal();

        // 2. Buscamos el chat en la base de datos
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));

        // 3. Inicializamos el conjunto por si es nulo (prevención de NullPointerException)
        if (conv.getHiddenBy() == null) {
            conv.setHiddenBy(new HashSet<>());
        }

        // 4. Te agregamos a la lista de personas que han ocultado este chat
        conv.getHiddenBy().add(currentUser);

        // 5. Guardamos los cambios en la BD
        conversationRepository.save(conv);
    }

    /**
     * LÓGICA: OBTENER TODAS LAS CONVERSACIONES
     * Se usa cuando entras a tu app y carga tu panel izquierdo.
     * Ahora filtra los chats que el usuario ha decidido ocultar.
     */
    public List<ConversationResponse> getUserConversation(Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();

        // 1. Buscamos todas las conversaciones donde tu ID esté en la tabla de participantes
        List<Conversation> conversations = conversationRepository.findAllByUserId(user.getId());

        // 2. Transformamos cada entidad Conversation en un ConversationResponse (DTO)
        return conversations.stream()
                // 🚀 FILTRO MÁGICO: Si la lista 'hiddenBy' te contiene, ignoramos este chat
                .filter(conv -> conv.getHiddenBy() == null || !conv.getHiddenBy().contains(user))
                // 3. Mapeamos las conversaciones que sí pasaron el filtro
                .map(conversation -> mapToResponse(conversation, user))
                .toList();
    }

    /**
     * LÓGICA: MAPEO DE ENTIDAD A DTO
     * Convierte la entidad Conversation en un objeto limpio (ConversationResponse)
     * Calculando inteligentemente el nombre del chat (Si es grupal o individual).
     */
    private ConversationResponse mapToResponse(Conversation conv, User currentUser) {

        // 1. Sacamos a todos los participantes de este chat, EXCEPTO a ti mismo
        List<User> otherUsers = conv.getParticipants().stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .toList();

        String chatName;

        // 2. Lógica para nombrar el chat dependiendo de la cantidad de personas
        if (otherUsers.isEmpty()) {
            chatName = currentUser.getNickName(); // Caso raro: te quedaste solo en el grupo
        } else if (otherUsers.size() == 1) {
            chatName = otherUsers.get(0).getNickName(); // Chat normal 1 vs 1
        } else {
            // 🚀 CHAT GRUPAL: Unimos los nombres de los demás separados por comas.
            // Ejemplo de salida: "Wumpus, Juan, Maria"
            chatName = otherUsers.stream()
                    .map(User::getNickName)
                    .collect(Collectors.joining(", "));
        }

        // 3. Construimos y devolvemos la respuesta final
        return ConversationResponse.builder()
                .id(conv.getId())
                .otherUserName(chatName) // Usamos el nombre dinámico que calculamos arriba
                .build();
    }
}