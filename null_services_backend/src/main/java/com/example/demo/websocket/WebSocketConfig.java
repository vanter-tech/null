package com.example.demo.websocket;

import com.example.demo.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * Configuración central del broker de
 * mensajes WebSocket utilizando STOMP.
 * Establece los endpoints de conexión, los prefijos
 * de destino y un interceptor
 * de seguridad para validar tokens JWT en el canal de entrada.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Orígenes permitidos para CORS, inyectados
     * desde la configuración de la aplicación.
     */
    @Value("${application.cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * Primeros 7 caracteres (Bearer ) para eliminar y
     * obtener el código jwt limpio.
     */
    private final int bearer = 7;

    /**
     * Servicio para la extracción y validación de tokens JWT.
     */
    @Autowired
    private JwtService jwtService;

    /**
     * Servicio para cargar los detalles del usuario
     * desde la base de datos.
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Registra los endpoints de STOMP que los clientes
     * utilizarán para conectar.
     *
     * @param registry El registro de endpoints de STOMP.
     */
    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    /**
     * Configura el broker de mensajes para manejar
     * tópicos y destinos de aplicación.
     *
     * @param registry El registro del broker de mensajes.
     */
    @Override
    public void configureMessageBroker(final MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Configura el canal de entrada con un interceptor que actúa como portero.
     * Atrapa el token JWT desde los headers de STOMP, lo valida y asigna
     * una identidad (Principal) a la sesión del WebSocket.
     *
     * @param registration El registro de la configuración del canal.
     */
    @Override
    public void configureClientInboundChannel(
            final ChannelRegistration registration) {

        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(
                    final Message<?> message,
                    final MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message,
                                StompHeaderAccessor.class);

                if (accessor != null
                        && StompCommand.CONNECT.equals(accessor.getCommand())) {

                    String authHeader = accessor.getFirstNativeHeader(
                            "Authorization");

                    if (authHeader != null
                            &&
                            authHeader.startsWith("Bearer ")) {

                        String token = authHeader.substring(bearer);

                        try {
                            String email = jwtService.extractUsername(token);

                            if (email != null) {
                                UserDetails user = userDetailsService
                                        .loadUserByUsername(email);

                                if (jwtService.isTokenValid(token, user)) {
                                    // Usamos el método de ayuda aquí abajo 👇
                                    accessor.setUser(createAuthToken(user));
                                    System.out.println("✅ WS: " + email);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("❌ WS Err: " + e.getMessage());
                        }
                    }
                }
                return message;
            }
        });
    }

    /**
     * Configura los límites de tamaño y tiempo
     * del transporte de WebSockets.
     *
     * @param registration El registro de
     * la configuración del transporte.
     */
    @Override
    public void configureWebSocketTransport(
            final WebSocketTransportRegistration registration) {
        final int messageSizeLimit = 128 * 1024;
        final int bufferSizeLimit = 1024 * 1024;
        final int timeLimit = 20000;

        registration.setMessageSizeLimit(messageSizeLimit);
        registration.setSendBufferSizeLimit(bufferSizeLimit);
        registration.setSendTimeLimit(timeLimit);
    }

    /**
     * Crea el token de autenticación para el WebSocket.
     * Método extraído para evitar superar
     * el límite de 80 caracteres.
     *
     * @param user Los detalles del usuario validado.
     * @return El token de autenticación configurado.
     */
    private UsernamePasswordAuthenticationToken createAuthToken(
            final UserDetails user) {
        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );
    }
}
