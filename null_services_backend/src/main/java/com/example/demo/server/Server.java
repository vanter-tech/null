package com.example.demo.server;

import com.example.demo.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

/**
 * Entidad que representa un Servidor o Comunidad dentro de la plataforma.
 * <p>
 * Un servidor actúa como un contenedor para múltiples usuarios y, en etapas posteriores,
 * para canales de texto y voz. Gestiona la propiedad del espacio (Owner) y la lista
 * de participantes autorizados (Members).
 * </p>
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Server {

    /**
     * Identificador único del servidor.
     * Se utiliza GenerationType.IDENTITY para delegar el autoincremento a la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del servidor (ej. "Comunidad de Devs").
     * Se marca como único para mantener la identidad clara dentro de la plataforma.
     */
    @Column(unique = true)
    private String name;

    /**
     * URL de la imagen o icono del servidor.
     * Almacena la ruta a un servicio de almacenamiento (ej. AWS S3 o Cloudinary).
     */
    private String imageUrl;

    /**
     * El usuario creador y administrador principal del servidor.
     * <p>
     * Relación Muchos-a-Uno: Un usuario puede ser dueño de múltiples servidores,
     * pero un servidor solo tiene un propietario legal en este modelo.
     * </p>
     */
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    /**
     * Conjunto de usuarios que forman parte del servidor.
     * <p>
     * Relación Muchos-a-Muchos: Se utiliza una tabla intermedia 'server_members'
     * para mapear qué usuarios pertenecen a qué servidores.
     * Se utiliza {@link Set} en lugar de List para garantizar que un mismo usuario
     * no pueda unirse al mismo servidor más de una vez (unicidad de miembros).
     * </p>
     */
    @ManyToMany
    @JoinTable(
            name = "server_members",
            joinColumns = @JoinColumn(name = "server_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members;

}