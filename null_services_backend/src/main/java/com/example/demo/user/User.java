package com.example.demo.user;

import com.example.demo.friends.Friends;
import com.example.demo.role.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entidad principal que representa a un usuario dentro del sistema.
 * <p>
 * Esta clase mapea a la tabla "_user" en la base de datos y actúa como el núcleo
 * para la autenticación y autorización implementando {@link UserDetails} y {@link Principal}.
 * También gestiona las relaciones clave del usuario, como sus roles y su lista de amigos.
 * </p>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "_user")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails, Principal {

    @Id
    @GeneratedValue
    private Integer id;

    /** * Correo electrónico del usuario.
     * Se utiliza como identificador único (username) para el inicio de sesión en Spring Security.
     */
    @Column(unique = true)
    private String email;

    private String fullname;
    private String nickName;
    private String password;
    private LocalDate dateOfBirth;

    // NUEVO: Campo para la futura foto de perfil
    private String imageUrl;

    /**
     * Estado actual de presencia del usuario.
     * <p>
     * Se guarda como STRING en la base de datos para evitar desajustes de índices
     * si el Enum se modifica en el futuro. Por defecto, todo usuario nace desconectado.
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.OFFLINE;

    // Banderas de control de acceso para Spring Security
    private boolean accountLocked;
    private boolean enable;

    /**
     * Roles asignados al usuario (ej. USER, ADMIN).
     * Se carga de forma temprana (EAGER) porque Spring Security necesita los roles
     * inmediatamente durante el proceso de autenticación.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Role> roles;

    /**
     * Lista de solicitudes de amistad enviadas por este usuario.
     * Si el usuario es eliminado, sus solicitudes enviadas también se eliminan en cascada.
     */
    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friends> sentFriendRequests = new ArrayList<>();

    /**
     * Lista de solicitudes de amistad recibidas por este usuario.
     * Si el usuario es eliminado, sus solicitudes recibidas también se eliminan en cascada.
     */
    @OneToMany(mappedBy = "addressee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friends> receivedFriendRequests = new ArrayList<>();

    // --- Campos de Auditoría gestionados automáticamente por JPA ---

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;


    // ========================================================================
    // MÉTODOS SOBRESCRITOS DE Principal Y UserDetails (SPRING SECURITY)
    // ========================================================================

    /**
     * @return El nombre del principal (identificador principal del usuario).
     * En este sistema, el email actúa como el identificador principal.
     */
    @Override
    public String getName() {
        return email;
    }

    /**
     * Convierte la lista de entidades {@link Role} del usuario en una colección de
     * {@link SimpleGrantedAuthority}, que es el formato que Spring Security entiende
     * para manejar los permisos.
     *
     * @return Colección de autoridades (roles) otorgadas al usuario.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * @return El nombre de usuario utilizado para autenticar.
     * Utilizamos el email como username en este sistema.
     */
    @Override
    public String getUsername() {
        return email;
    }

    // --- Banderas de estado de la cuenta ---

    @Override
    public boolean isAccountNonExpired() {
        return true; // Por defecto, las cuentas no expiran en este sistema
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Por defecto, las credenciales (contraseñas) no expiran
    }

    @Override
    public boolean isEnabled() {
        return enable; // Controlado por el flujo de activación (ej. confirmación por email)
    }
}