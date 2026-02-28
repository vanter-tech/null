package com.example.demo.role;

import com.example.demo.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad que representa un Rol de seguridad en el sistema (ej. "USER", "ADMIN").
 * <p>
 * Fundamental para el Control de Acceso Basado en Roles (RBAC). Se utiliza en
 * conjunto con Spring Security para determinar qué rutas o acciones están permitidas
 * para un usuario específico.
 * </p>
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Role {

    @Id
    @GeneratedValue
    private Integer id;

    /**
     * El nombre del rol (ej. "USER", "ADMIN", "MODERATOR").
     * Se define como único para evitar duplicados en la base de datos y mantener
     * la integridad de los permisos.
     */
    @Column(unique = true)
    private String name;

    /**
     * Relación Inversa (Many-to-Many) con los usuarios.
     * <p>
     * NOTA DE ARQUITECTURA: La anotación @JsonIgnore es CRÍTICA aquí.
     * Dado que un Usuario tiene Roles, y un Rol tiene Usuarios, si intentamos
     * devolver un Rol en un endpoint REST, Jackson intentaría serializar la lista
     * de usuarios, que a su vez serializaría sus roles, creando un bucle infinito
     * (StackOverflowError). @JsonIgnore rompe ese ciclo bidireccional.
     * </p>
     * Además, 'mappedBy' indica que la entidad User es la dueña de la relación en la base de datos.
     */
    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private List<User> users;

    // --- Campos de Auditoría gestionados automáticamente por JPA ---

    /**
     * Fecha exacta en la que se creó este rol en la base de datos.
     * No se puede actualizar una vez insertado (updatable = false).
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * Fecha de la última modificación de los datos de este rol.
     * Spring Data JPA actualiza esto automáticamente gracias al AuditingEntityListener.
     */
    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

}