package com.example.demo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de la gestión de perfiles y configuraciones de los usuarios.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Actualiza el estado de presencia visual del usuario en la plataforma.
     * <p>
     * Permite implementar el modo "Invisible" (OFFLINE) o estados personalizados
     * (AWAY, DO_NOT_DISTURB) sin cerrar la conexión real del usuario.
     * </p>
     *
     * @param newStatus El nuevo estado seleccionado por el usuario.
     * @param connectedUser El usuario autenticado que realiza la petición.
     */
    public void updatePresenceStatus(UserStatus newStatus, Authentication connectedUser) {
        // Extraemos al usuario directamente del token JWT actual
        User user = (User) connectedUser.getPrincipal();

        // Actualizamos el estado
        user.setStatus(newStatus);

        // Guardamos los cambios en la base de datos
        userRepository.save(user);
    }
}