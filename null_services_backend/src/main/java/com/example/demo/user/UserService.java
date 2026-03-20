package com.example.demo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de gestionar la lógica de negocio
 * relacionada con los usuarios.
 * Maneja operaciones críticas como la actualización
 * del estado de presencia (Online/Offline)
 * y la notificación en tiempo real a través de WebSockets.
 */
@Service
@RequiredArgsConstructor
public final class UserService {

    /**
     * Repositorio para acceder y modificar los datos
     * de los usuarios en la base de datos.
     */
    private final UserRepository userRepository;

    /**
     * Plantilla de mensajería de Spring utilizada
     * para enviar eventos en tiempo real
     * a través de WebSockets (STOMP) a los clientes conectados.
     */
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Actualiza el estado de presencia de un
     * usuario y emite un evento WebSocket para notificar
     * a los demás clientes sobre el cambio de estado.
     *
     * @param newStatus El nuevo estado del usuario
     * (por ejemplo, ONLINE, OFFLINE).
     * @param connectedUser El usuario autenticado que
     * está cambiando su estado.
     */
    public void updatePresenceStatus(
            final UserStatus newStatus,
            final Authentication connectedUser) {

        User user = (User) connectedUser.getPrincipal();
        user.setStatus(newStatus);
        userRepository.save(user);

        String destination = "/topic/user/status/" + user.getId();

        messagingTemplate.convertAndSend(destination, newStatus.toString());

        // Trazabilidad básica del evento emitido
        System.out.println("WebSocket emitido a: "
                +
                destination + " con valor: " + newStatus);
    }
}
