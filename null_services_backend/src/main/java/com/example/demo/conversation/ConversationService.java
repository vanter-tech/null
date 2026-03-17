package com.example.demo.conversation;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    /**
     * Repositorio para acceder a las conversaciones
     * de los usuarios.
     */
    private final ConversationRepository conversationRepository;
    /**
     * Repositorio para encontrar a
     * los usuarios en la base de datos a traves del
     * email.
     */
    private final UserRepository userRepository;

    /**
     * LÓGICA: CHAT 1 VS 1
     * Busca si ya existe un chat entre tú y tu amigo. Si existe, lo devuelve.
     * Si no existe, crea uno nuevo.
     * @param connectedUser Usuario activo validado desde el token
     * de seguridad
     * @param targetUserId Usuario seleccionado para crear u
     * obtener la conversación.
     * @return si el primer condicional es verdadero se devuelve
     * el chat de la persona pero si es negativo se activa el siguiente y
     * se crea el chat con el usuario.
     */
    public ConversationResponse createOrGetConversation(
            final Integer targetUserId,
            final Authentication connectedUser
    ) {
        // 1. Obtenemos quién eres tú desde el token de seguridad
        User currentUser = (User) connectedUser.getPrincipal();

        // 2. Regla de negocio: Evitar crear un chat contigo mismo
        if (currentUser.getId().equals(targetUserId)) {
            throw new RuntimeException(
                    "No puedes crear una conversación contigo mismo"
            );
        }

        // 3. Buscamos en la BD si ya tienen un chat previo
        Optional<Conversation> existingConversation = conversationRepository
                .findConversationByUsers(currentUser.getId(), targetUserId);

        // 4. Si ya existe, simplemente devolvemos ese chat
        // (no creamos duplicados)
        if (existingConversation.isPresent()) {
            return mapToResponse(existingConversation.get(), currentUser);
        }

        // 5. Si no existe, buscamos al amigo en la BD
        // para asegurarnos de que sea real
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado"
                ));

        // 6. Creamos la nueva conversación
        // y metemos a ambos en el Set (conjunto)
        Conversation newConversation = Conversation.builder()
                .participants(Set.of(currentUser, targetUser))
                .build();

        // 7. Guardamos en BD y devolvemos la respuesta mapeada
        Conversation savedConversation = conversationRepository
                .save(newConversation);
        return mapToResponse(savedConversation, currentUser);
    }

    /**
     * LÓGICA: CHAT GRUPAL
     * Recibe una lista de IDs, los busca a todos, te incluye a ti en la lista,
     * y crea una sola conversación que los vincula a todos.
     * @param connectedUser Usuario activo validado desde el token
     * de seguridad
     * @param targetUserIds Usuarios seleccionados para crear u
     * obtener la conversación en grupo.
     * @return Al usuario que está creando el grupo y la conversación
     * con todos sus participantes ya guardados en una tabla.
     */
    public ConversationResponse createGroupConversation(
            final List<Integer> targetUserIds,
            final Authentication connectedUser
    ) {
        // 1. Obtenemos tu usuario
        User currentUser = (User) connectedUser.getPrincipal();

        // 2. Buscamos a todos los amigos enviados desde el modal de Angular
        List<User> targetUsers = userRepository.findAllById(targetUserIds);

        // 3. Validación de seguridad
        if (targetUsers.isEmpty()) {
            throw new RuntimeException(
                    "No se encontraron usuarios válidos para el grupo"
            );
        }

        // 4. Creamos un Set vacío y agregamos a todos los amigos
        Set<User> participants = new HashSet<>(targetUsers);

        // 5. Te agregamos a ti (el creador) al grupo
        participants.add(currentUser);

        // 6. Construimos la entidad de la
        // conversación con todos los participantes
        Conversation newConversation = Conversation.builder()
                .participants(participants)
                .build();

        // 7. Guardamos en BD. Hibernate creará
        // los registros en la tabla intermedia automáticamente.
        Conversation savedConversation = conversationRepository
                .save(newConversation);

        return mapToResponse(savedConversation, currentUser);
    }

    /**
     * LÓGICA: OCULTAR CONVERSACIÓN.
     * Oculta un chat (1v1 o grupal) de la vista del usuario actual,
     * añadiéndolo a la "lista negra" visual (hiddenBy)
     * @param connectedUser Usuario activo validado desde el token
     * de seguridad
     * @param conversationId Identificador unico de la conversación.
     */
    @Transactional
    public void hideConversation(
            final Long conversationId,
            final Authentication connectedUser
    ) {
        // 1. Obtenemos tu usuario desde el token de seguridad
        User currentUser = (User) connectedUser.getPrincipal();

        // 2. Buscamos el chat en la base de datos
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException(
                        "Conversación no encontrada"
                ));

        // 3. Inicializamos el conjunto por
        // si es nulo (prevención de NullPointerException)
        if (conv.getHiddenBy() == null) {
            conv.setHiddenBy(new HashSet<>());
        }

        // 4. Te agregamos a la lista de personas
        // que han ocultado este chat
        conv.getHiddenBy().add(currentUser);

        // 5. Guardamos los cambios en la BD
        conversationRepository.save(conv);
    }

    /**
     * LÓGICA: OBTENER TODAS LAS CONVERSACIONES
     * Se usa cuando entras a tu app y carga tu panel izquierdo.
     * Ahora filtra los chats que el usuario
     * ha decidido ocultar, comparando por ID de usuario.
     * @param connectedUser Usuario activo validado desde el token
     * de seguridad
     * @return Una lista de conversaciones formateadas
     * (ConversationResponse) visibles para el usuario.
     */
    public List<ConversationResponse> getUserConversation(
            final Authentication connectedUser
    ) {
        User user = (User) connectedUser.getPrincipal();

        // 1. Buscamos todas las conversaciones
        // donde tu ID esté en la tabla de participantes
        List<Conversation> conversations = conversationRepository
                .findAllByUserId(user.getId());

        // 2. Transformamos cada entidad Conversation
        // en un ConversationResponse (DTO)
        return conversations.stream()
                // FILTRO CORREGIDO: Verificamos si ningún usuario
                // en la lista de ocultos tiene tu mismo ID
                .filter(conv -> conv.getHiddenBy() == null
                        ||
                        conv.getHiddenBy().stream().noneMatch(
                                hiddenUser -> hiddenUser.getId().equals(
                                        user.getId())
                        )
                )
                // 3. Mapeamos las conversaciones que sí pasaron el filtro
                .map(conversation -> mapToResponse(conversation, user))
                .toList();
    }

    /**
     * LÓGICA: MAPEO DE ENTIDAD A DTO
     * Convierte la entidad Conversation en un
     * objeto limpio (ConversationResponse)
     * Calculando inteligentemente el nombre del chat
     * (Si es grupal o individual).
     *
     * @param conv La entidad de conversación obtenida
     * de la base de datos.
     * @param currentUser El usuario actual para el cual se
     * está calculando el nombre del chat.
     * @return Un objeto ConversationResponse con
     * los datos listos para el frontend.
     *
     */
    private ConversationResponse mapToResponse(
            final Conversation conv,
            final User currentUser
    ) {

        // 1. Sacamos a todos los participantes de este chat, EXCEPTO a ti mismo
        List<User> otherUsers = conv.getParticipants().stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .toList();

        String chatName;

        // 2. Lógica para nombrar el chat dependiendo de la cantidad de personas
        if (otherUsers.isEmpty()) {
            // Caso raro: te quedaste solo en el grupo
            chatName = currentUser.getNickName();
        } else if (otherUsers.size() == 1) {
            // Chat normal 1 vs 1
            chatName = otherUsers.get(0).getNickName();
        } else {
            // CHAT GRUPAL: Unimos los nombres de los demás separados por comas.
            // Ejemplo de salida: "Wumpus, Juan, Maria"
            chatName = otherUsers.stream()
                    .map(User::getNickName)
                    .collect(Collectors.joining(", "));
        }

        // 3. Construimos y devolvemos la respuesta final
        return ConversationResponse.builder()
                .id(conv.getId())
                // Usamos el nombre dinámico que calculamos arriba
                .otherUserName(chatName)
                .build();
    }
}
