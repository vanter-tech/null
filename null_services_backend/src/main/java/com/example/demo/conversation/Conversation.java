package com.example.demo.conversation;

import com.example.demo.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa una conversación (Mensaje Directo o Grupo).
 * <p>
 * Almacena la relación de los participantes activos en el chat y también
 * gestiona qué usuarios han decidido ocultar la conversación de su interfaz.
 * NOTA DE ARQUITECTURA: Se utilizan relaciones ManyToMany con tablas intermedias
 * separadas para mantener la integridad de los datos. La tabla 'conversation_hidden'
 * permite el comportamiento asíncrono donde un usuario oculta el chat sin afectar al resto.
 * </p>
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "conversation_participant",
            joinColumns = @JoinColumn(name = "conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants;

    @ManyToMany
    @JoinTable(
            name = "conversation_hidden",
            joinColumns = @JoinColumn(name = "conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> hiddenBy = new HashSet<>();

}