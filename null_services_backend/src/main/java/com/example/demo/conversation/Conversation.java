package com.example.demo.conversation;

import com.example.demo.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Almacena la relación de los participantes activos
 * en el chat y también
 * gestiona qué usuarios han decidido ocultar
 * la conversación de su interfaz.
 * NOTA DE ARQUITECTURA: Se utilizan relaciones
 * ManyToMany con tablas intermedias
 * separadas para mantener la integridad de los datos.
 * La tabla 'conversation_hidden'
 * permite el comportamiento asíncrono donde un usuario
 * oculta el chat sin afectar al resto.
 * </p>
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Conversation {

    /**
     * Identificador único de la entidad en la base de datos.
     * Su valor es autogenerado automáticamente de forma secuencial.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Conjunto de usuarios que participan en esta conversación.
     * Representa una relación de muchos a muchos, gestionada
     * mediante la tabla intermedia "conversation_participant".
     */
    @ManyToMany
    @JoinTable(
            name = "conversation_participant",
            joinColumns = @JoinColumn(name = "conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants;

    /**
     * Conjunto de usuarios que han decidido ocultar esta conversación
     * de su bandeja principal.
     * Representa una relación de muchos a muchos, gestionada
     * mediante la tabla intermedia "conversation_hidden".
     */
    @ManyToMany
    @JoinTable(
            name = "conversation_hidden",
            joinColumns = @JoinColumn(name = "conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> hiddenBy = new HashSet<>();

}
