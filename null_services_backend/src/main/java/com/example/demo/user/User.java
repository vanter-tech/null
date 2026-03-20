package com.example.demo.user;

import com.example.demo.friends.Friends;
import com.example.demo.role.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
 * Entidad principal que representa a un usuario
 * dentro del sistema.
 *
 * Esta clase mapea a la tabla "_user" en la
 * base de datos y actúa como el núcleo
 * para la autenticación y autorización implementando
 * UserDetails y Principal.
 * También gestiona las relaciones clave del usuario, como sus
 * roles y su lista de amigos.
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

    /**
     * Identificador único del usuario generado
     * automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue
    private Integer id;

    /**
     * Correo electrónico del usuario.
     * Se utiliza como identificador único (username)
     * para el inicio de sesión en Spring Security.
     */
    @Column(unique = true)
    private String email;

    /**
     * Nombre completo del usuario.
     */
    private String fullname;

    /**
     * Apodo o nickname que el usuario mostrará
     * en la interfaz gráfica.
     */
    private String nickName;

    /**
     * Contraseña encriptada del usuario
     * (generalmente usando BCrypt).
     */
    private String password;

    /**
     * Fecha de nacimiento del usuario.
     */
    private LocalDate dateOfBirth;

    /**
     * URL de la imagen de perfil o avatar del usuario.
     */
    private String imageUrl;

    /**
     * Estado actual de presencia del usuario.
     * Se guarda como STRING en la base de datos
     * para evitar desajustes de índices
     * si el Enum se modifica en el futuro. Por defecto,
     * todo usuario nace desconectado.
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.OFFLINE;

    /**
     * Bandera de control de acceso que indica
     * si la cuenta ha sido bloqueada.
     */
    private boolean accountLocked;

    /**
     * Bandera que indica si la cuenta del usuario
     * está activa y verificada.
     */
    private boolean enable;

    /**
     * Roles asignados al usuario (ejemplo: USER, ADMIN).
     * Se carga de forma temprana (EAGER) porque
     * Spring Security necesita los roles
     * inmediatamente durante el proceso de autenticación.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Role> roles;

    /**
     * Lista de solicitudes de amistad enviadas por este usuario.
     * Si el usuario es eliminado, sus solicitudes
     * enviadas también se eliminan en cascada.
     */
    @OneToMany(mappedBy = "requester",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friends> sentFriendRequests = new ArrayList<>();

    /**
     * Lista de solicitudes de amistad recibidas por este usuario.
     * Si el usuario es eliminado, sus solicitudes
     * recibidas también se eliminan en cascada.
     */
    @OneToMany(mappedBy = "addressee",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friends> receivedFriendRequests = new ArrayList<>();

    // --- Campos de Auditoría gestionados automáticamente por JPA ---

    /**
     * Fecha exacta en la que se creó el registro del usuario.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * Fecha de la última modificación de los datos de este usuario.
     */
    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

    // ========================================================================
    // MÉTODOS SOBRESCRITOS DE Principal Y UserDetails (SPRING SECURITY)
    // ========================================================================

    /**
     * Obtiene el nombre del principal (identificador principal del usuario).
     * En este sistema, el email actúa como el identificador principal.
     *
     * @return El correo electrónico del usuario.
     */
    @Override
    public String getName() {
        return email;
    }

    /**
     * Convierte la lista de entidades Role del
     * usuario en una colección de
     * SimpleGrantedAuthority, que es el formato que Spring Security
     * entiende para manejar los permisos.
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

    /**
     * Obtiene la contraseña del usuario.
     *
     * @return La contraseña encriptada.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Obtiene el nombre de usuario utilizado para autenticar.
     *
     * @return El correo electrónico del usuario.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Verifica si la cuenta del usuario ha expirado.
     *
     * @return true, ya que por defecto
     * las cuentas no expiran en este sistema.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Verifica si la cuenta del usuario no está bloqueada.
     *
     * @return true si la cuenta no está bloqueada,
     * false en caso contrario.
     */
    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    /**
     * Verifica si las credenciales (contraseña) del usuario no han expirado.
     *
     * @return true, ya que por defecto las credenciales no expiran.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Verifica si el usuario está habilitado en el sistema.
     *
     * @return El valor de la bandera enable
     * (controlada por confirmación de email, etc.).
     */
    @Override
    public boolean isEnabled() {
        return enable;
    }
}
