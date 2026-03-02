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

}