package com.example.demo.friends;

import com.example.demo.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa una relación de
 * amistad o una solicitud de amistad entre dos usuarios.
 * Se mapea a la tabla "friends" en la base de datos.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "friends")
public class Friends {

    /**
     * Identificador único de la relación de amistad en la base de datos.
     * Generado automáticamente de forma secuencial.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que envía la solicitud de amistad.
     */
    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    /**
     * Usuario que recibe la solicitud de amistad.
     */
    @ManyToOne
    @JoinColumn(name = "addresse_id")
    private User addressee;

    /**
     * Estado actual de la relación (ej. PENDIENTE, ACEPTADA, RECHAZADA).
     */
    @Enumerated(EnumType.STRING)
    private FriendShipStatus status;

    /**
     * Fecha y hora en la que se creó la solicitud o relación de amistad.
     */
    private LocalDateTime createdAt;

}
