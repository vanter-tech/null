package com.example.demo.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository
        extends JpaRepository<Conversation, Long> {

    /**
     * Recupera todas las conversaciones en las que
     * participa un usuario específico.
     *
     * @param userId El identificador único
     * del usuario que se desea buscar.
     * @return Una lista con todas las
     * conversaciones asociadas a ese usuario.
     */
    @Query(
            "SELECT c FROM Conversation c "
                    +
                    "JOIN c.participants p WHERE p.id = :userId"
    )
    List<Conversation> findAllByUserId(@Param("userId") Integer userId);

    /**
     * Busca una conversación directa y
     * exclusiva entre dos usuarios.
     * Válida que ambos participen y que la
     * conversación tenga exactamente dos integrantes.
     *
     * @param userId El identificador único del primer usuario.
     * @param user2Id El identificador único del segundo usuario.
     * @return Un Optional que contiene la conversación si existe,
     * o vacío si aún no han chateado.
     */
    @Query("SELECT c FROM Conversation c JOIN c.participants p1 "
            +
            "JOIN c.participants p2 WHERE p1.id = :userId "
            +
            "AND p2.id = :user2Id "
            +
            "AND size(c.participants) = 2")
    Optional<Conversation> findConversationByUsers(
            @Param("userId") Integer userId, @Param("user2Id") Integer user2Id);

}
