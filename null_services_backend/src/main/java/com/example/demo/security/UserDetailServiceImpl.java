package com.example.demo.security;

import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación personalizada del servicio
 * central de usuarios de Spring Security.
 * <p>
 * Actúa como un puente entre la base de datos de
 * la aplicación (vía UserRepository)
 * y el ecosistema de seguridad de Spring.
 * Su única responsabilidad es localizar
 * el registro del usuario y devolverlo en un formato
 * que Spring Security entienda
 * (la interfaz UserDetails).
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    /**
     * Repositorio para acceder a la información de
     * los usuarios.
     */
    private final UserRepository repository;

    /**
     * Busca y carga los datos de un usuario
     * específico durante el proceso de autenticación
     * o validación de tokens JWT.
     * <p>
     * NOTA DE ARQUITECTURA: Aunque el método
     * de la interfaz obliga a usar el nombre
     * 'loadUserByUsername', en esta arquitectura
     * el identificador principal (username)
     * es siempre el correo electrónico (email).
     * </p>
     *
     * @param userEmail El correo electrónico del
     * usuario a autenticar, extraído del formulario
     * de login o del payload del token JWT.
     * @return El objeto {@link UserDetails}
     * completamente poblado con los datos,
     * contraseña hasheada y roles del usuario.
     * @throws UsernameNotFoundException Si el correo
     * electrónico no existe en la base de datos,
     * deteniendo inmediatamente el flujo de autenticación.
     */
    @Override
    public UserDetails loadUserByUsername(
            final String userEmail)
            throws UsernameNotFoundException {
        return repository.findByEmail(userEmail)
                // Usamos una excepción específica de
                // Spring Security para que el framework
                // sepa exactamente cómo traducirla a un error
                // HTTP 401/403 (Unauthorized/Forbidden)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found::" + userEmail));
    }
}
