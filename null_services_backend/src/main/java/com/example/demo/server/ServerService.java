package com.example.demo.server;

import com.example.demo.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio encargado de orquestar la lógica de negocio para los Servidores.
 * <p>
 * Gestiona la creación de comunidades, la recuperación de las mismas y la
 * transformación de las entidades persistentes en objetos de respuesta (DTOs).
 * Actúa como intermediario entre el controlador y la capa de datos.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ServerService {

    private final ServerRepository serverRepository;

    /**
     * Crea un nuevo servidor en el sistema y asigna al creador como dueño y miembro.
     * <p>
     * NOTA DE ARQUITECTURA: Al usar {@link Set#of(Object)}, garantizamos que el servidor
     * inicie con al menos un miembro (el propietario). Esto es vital para que las
     * consultas de "mis servidores" funcionen correctamente desde el minuto uno.
     * </p>
     *
     * @param request DTO con la información básica del servidor (nombre, imagen).
     * @param connectedUser Objeto de autenticación del usuario que solicita la creación.
     * @return {@link ServerResponse} con los datos del servidor persistido.
     */

    public ServerResponse createServer(ServerRequest request, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();

        Server newServer = Server.builder()
                .name(request.getName())
                .imageUrl(request.getImageUrl())
                .owner(user)
                .members(Set.of(user))
                .build();

        // 🚀 Le creamos su canal por defecto
        Channel defaultChannel = Channel.builder()
                .name("general")
                .type("TEXT")
                .server(newServer)
                .build();

        newServer.setChannels(List.of(defaultChannel));

        Server savedServer = serverRepository.save(newServer);
        return mapToResponse(savedServer);
    }

    /**
     * Recupera todos los servidores registrados en la plataforma.
     * <p>
     * Ideal para funciones de "Explorar" o "Descubrir" donde se muestran
     * comunidades públicas a los usuarios.
     * </p>
     *
     * @return Lista de todos los servidores mapeados a sus respuestas DTO.
     */
    public List<ServerResponse> findAll() {
        return serverRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la lista de servidores a los que el usuario autenticado pertenece.
     * <p>
     * Utiliza el ID extraído del token JWT para filtrar en la base de datos a través
     * de la relación Many-to-Many.
     * </p>
     *
     * @param connectedUser Usuario autenticado que realiza la consulta.
     * @return Lista de servidores asociados al usuario.
     */
    public List<ServerResponse> findByUser(Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        return serverRepository.findAllByMembersId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Método helper privado para transformar una Entidad Server en un DTO ServerResponse.
     * <p>
     * Este mapeo manual asegura que no enviemos datos internos de la base de datos
     * ni relaciones circulares hacia el Frontend.
     * </p>
     *
     * @param server La entidad a transformar.
     * @return El objeto de respuesta listo para JSON.
     */

    private ServerResponse mapToResponse(Server server) {
        // Mapeamos la lista de Entidades Channel a ChannelResponse
        List<ChannelResponse> channelResponses = server.getChannels() != null
                ? server.getChannels().stream()
                .map(channel -> ChannelResponse.builder()
                        .id(channel.getId())
                        .name(channel.getName())
                        .type(channel.getType())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        List<MemberResponse> memberResponses = server.getMembers() != null
                ? server.getMembers().stream()
                // Asegúrate de usar los métodos correctos de tu entidad User (getNickname, getUsername, etc.)
                .map(member -> MemberResponse.builder()
                        .id(member.getId())
                        .username(member.getUsername()) // O el campo que uses en tu User
                        .imageUrl(member.getImageUrl()) // Si tienes avatar
                        .status(member.getStatus() != null ? member.getStatus().name() : "OFFLINE")
                        .build())
                .collect(Collectors.toList())
                : List.of();

        return ServerResponse.builder()
                .id(server.getId())
                .name(server.getName())
                .imageUrl(server.getImageUrl())
                .ownerId(server.getOwner().getId())
                .channels(channelResponses)
                .members(memberResponses)// Agregamos los canales al DTO
                .build();
    }

    public ServerResponse findById(Long serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));
        return mapToResponse(server);
    }

    /**
     * Permite a un usuario unirse a un servidor existente.
     *
     * @param serverId ID del servidor al que se quiere unir.
     * @param connectedUser El usuario autenticado actual.
     * @return El servidor actualizado.
     */
    public ServerResponse joinServer(Long serverId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();

        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("¡Servidor no encontrado!"));

        // Añadimos el usuario al Set de miembros (como es un Set, si ya está, no se duplica)
        server.getMembers().add(user);

        Server savedServer = serverRepository.save(server);

        return mapToResponse(savedServer);
    }

    /**
     * Permite a un usuario abandonar un servidor.
     * Si el usuario es el creador (owner), no se le permite abandonar.
     *
     * @param serverId ID del servidor a abandonar.
     * @param connectedUser El usuario que hace la petición.
     */
    public void leaveServer(Long serverId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();

        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("¡Servidor no encontrado!"));

        // 🚀 Regla de negocio: El dueño no puede abandonar su propio barco
        if (server.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("El propietario no puede abandonar el servidor. Debes eliminarlo.");
        }

        // 🚀 Removemos al usuario de la lista de miembros
        server.getMembers().removeIf(member -> member.getId().equals(user.getId()));

        serverRepository.save(server);
    }

    /**
     * Crea un nuevo canal dentro de un servidor existente.
     * 🚀 SOLO EL DUEÑO PUEDE HACER ESTO.
     */
    public ChannelResponse createChannel(Long serverId, ChannelRequest request, Authentication connectedUser) {
        // Obtenemos al usuario que hace la petición
        User user = (User) connectedUser.getPrincipal();

        // 1. Buscamos el servidor
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        // 🚀 CANDADO DE SEGURIDAD: Validamos que el usuario sea el dueño
        if (!server.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Solo el dueño puede crear canales en este servidor.");
        }

        // 2. Formateamos el nombre (minúsculas y guiones, estilo Discord)
        String formattedName = request.getName().trim().toLowerCase().replaceAll("\\s+", "-");

        // 3. Creamos la entidad Channel
        Channel newChannel = Channel.builder()
                .name(formattedName)
                .type(request.getType() != null ? request.getType() : "TEXT")
                // 🚀 Protección extra contra nulos por si acaso
                .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false)
                .server(server)
                .build();

        // 4. Lo añadimos a la lista del servidor y guardamos
        server.getChannels().add(newChannel);
        serverRepository.save(server);

        // 5. Retornamos la respuesta
        return ChannelResponse.builder()
                .id(newChannel.getId())
                .name(newChannel.getName())
                .type(newChannel.getType())
                .isPrivate(newChannel.getIsPrivate()) // Añadimos esto para que Angular lo sepa
                .build();
    }

    /**
     * Elimina permanentemente un servidor y todos sus canales asociados.
     * 🚀 SOLO EL DUEÑO PUEDE HACER ESTO.
     *
     * @param serverId ID del servidor a eliminar.
     * @param connectedUser El usuario que hace la petición.
     */
    public void deleteServer(Long serverId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        // 🚀 CANDADO DE SEGURIDAD: Validamos que solo el dueño pueda borrarlo
        if (!server.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Acceso denegado: Solo el propietario puede eliminar este servidor.");
        }

        // Eliminamos el servidor.
        // Nota: Si tienes configurado CascadeType.ALL en tu entidad Server (hacia canales),
        // esto borrará automáticamente todos los canales asociados.
        serverRepository.delete(server);
    }
}