package com.example.demo.server; // Ajusta el paquete según tu estructura

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa un Canal dentro de un Servidor.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "channels")
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del canal (ej. "general", "anuncios").
     */
    @Column(nullable = false)
    private String name;

    /**
     * Tipo de canal para diferenciar si es de texto o de voz.
     * Por ahora lo manejamos como String (ej. "TEXT", "VOICE").
     */
    @Column(nullable = false)
    private String type = "TEXT";

    /**
     * El servidor al que pertenece este canal.
     * <p>
     * Relación Muchos-a-Uno: Muchos canales apuntan a un solo servidor.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

}