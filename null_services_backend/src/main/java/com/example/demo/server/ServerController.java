package com.example.demo.server;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST encargado de gestionar las operaciones de los Servidores/Comunidades.
 * <p>
 * Expone endpoints para el descubrimiento de nuevos servidores, la consulta de servidores
 * a los que pertenece el usuario y la creación de nuevos espacios comunitarios.
 * </p>
 */
@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerController {

    private final ServerService serverService;

    /**
     * Recupera una lista de todos los servidores disponibles en la plataforma.
     * <p>
     * Este endpoint se utiliza para la sección de "Descubrimiento", permitiendo
     * a los usuarios encontrar comunidades a las que unirse.
     * </p>
     *
     * @return ResponseEntity con la lista de servidores mapeados a {@link ServerResponse}.
     */
    @GetMapping("/discover")
    public ResponseEntity<List<ServerResponse>> findAllServer(){
        return ResponseEntity.ok(serverService.findAll());
    }

    /**
     * Recupera la lista de servidores donde el usuario actual es miembro o propietario.
     * <p>
     * Utiliza el objeto {@link Authentication} inyectado por Spring Security para
     * obtener de forma segura la identidad del usuario desde el token JWT.
     * </p>
     *
     * @param connectedUser Objeto que representa al usuario autenticado actual.
     * @return ResponseEntity con la lista de servidores del usuario.
     */
    @GetMapping("/my-servers")
    public ResponseEntity<List<ServerResponse>> findMyServers(
            Authentication connectedUser
    ){
        return ResponseEntity.ok(serverService.findByUser(connectedUser));
    }

    /**
     * Crea un nuevo servidor en la plataforma.
     * <p>
     * El usuario que realiza la petición será asignado automáticamente como el
     * 'Owner' (propietario) del servidor. Se aplica validación sobre el cuerpo
     * de la petición mediante @Valid.
     * </p>
     *
     * @param request Datos del nuevo servidor (nombre, imagen, etc.).
     * @param connectedUser El usuario que será dueño del servidor.
     * @return ResponseEntity con los detalles del servidor recién creado.
     */
    @PostMapping
    public ResponseEntity<ServerResponse> saveServer(
            @RequestBody @Valid ServerRequest request,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(serverService.createServer(request, connectedUser));
    }

    /**
     * Recupera toda la información de un servidor específico, incluyendo sus canales.
     * <p>
     * Se invoca cuando el usuario hace clic en el icono de un servidor en la barra lateral.
     * </p>
     *
     * @param serverId El ID del servidor a consultar.
     * @return ResponseEntity con los detalles del servidor y sus canales.
     */
    @GetMapping("/{serverId}")
    public ResponseEntity<ServerResponse> findServerById(
            @PathVariable Long serverId
    ){
        return ResponseEntity.ok(serverService.findById(serverId));
    }

    /**
     * Endpoint para que un usuario se una a una comunidad.
     *
     * @param serverId El ID del servidor.
     * @param connectedUser El usuario que hace la petición.
     */
    @PostMapping("/{serverId}/join")
    public ResponseEntity<ServerResponse> joinServer(
            @PathVariable Long serverId,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(serverService.joinServer(serverId, connectedUser));
    }

    /**
     * Endpoint para que un usuario abandone una comunidad.
     *
     * @param serverId El ID del servidor a abandonar.
     */
    @PostMapping("/{serverId}/leave")
    public ResponseEntity<Void> leaveServer(
            @PathVariable Long serverId,
            Authentication connectedUser
    ){
        serverService.leaveServer(serverId, connectedUser);
        return ResponseEntity.ok().build();
    }

    /**
     * Crea un nuevo canal en un servidor específico.
     *
     * @param serverId El ID del servidor padre.
     * @param request Los datos del canal (nombre, tipo).
     * @return El canal recién creado.
     */
    // 🚀 ACTUALIZADO: Añadimos Authentication
    @PostMapping("/{serverId}/channels")
    public ResponseEntity<ChannelResponse> createChannel(
            @PathVariable Long serverId,
            @RequestBody @Valid ChannelRequest request,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(serverService.createChannel(serverId, request, connectedUser));
    }

    // 🚀 NUEVO: Endpoint para borrar el servidor
    @DeleteMapping("/{serverId}")
    public ResponseEntity<Void> deleteServer(
            @PathVariable Long serverId,
            Authentication connectedUser
    ){
        serverService.deleteServer(serverId, connectedUser);
        return ResponseEntity.noContent().build();
    }

}